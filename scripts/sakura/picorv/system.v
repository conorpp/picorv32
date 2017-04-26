`timescale 1 ns / 1 ps

module system (
	input            clk,
	input            resetn,
	input            [1:0]M_PUSHSW,
    input            uart_rxd,
    output           uart_txd,
	output           trap,
	output reg [7:0] out_byte,
	output reg       out_byte_en,
    output [9:0] M_LED
);
	// set this to 0 for better timing but less performance/MHz
	parameter FAST_MEMORY = 0;

	// 4096 32bit words = 16kB memory
	parameter MEM_SIZE = 4096;

	wire mem_valid;
	wire mem_instr;
	reg mem_ready;
	wire [31:0] mem_addr;
	wire [31:0] mem_wdata;
	wire [3:0] mem_wstrb;
	reg [31:0] mem_rdata;

    //wire [7:0] outb = (M_PUSHSW[0] == 1) ? (memory[mem_addr >> 2][7:0]) : out_byte;
    assign M_LED = {memory[mem_addr >> 2][1:0], out_byte};
    //assign M_LED = {1'b0, resetn, out_byte};

    picorv32 
    #(
        .ENABLE_REGS_DUALPORT(0)
    )picorv32_core (
        .clk         (clk     ),
        .resetn      (resetn      ),
        .trap        (trap        ),
        .mem_valid   (mem_valid   ),
        .mem_instr   (mem_instr   ),
        .mem_ready   (mem_ready   ),
        .mem_addr    (mem_addr    ),
        .mem_wdata   (mem_wdata   ),
        .mem_wstrb   (mem_wstrb   ),
        .mem_rdata   (mem_rdata   )
    );

    reg [7:0] uart_tx_axi_tdata;
    reg uart_tx_axi_tvalid;
    wire uart_tx_axi_tready;

    wire [7:0] uart_rx_axi_tdata;
    wire uart_rx_axi_tvalid;
    reg uart_rx_axi_tready;

    uart u(.clk(clk), .rst(~resetn),
        // axi input
        .input_axis_tdata(uart_tx_axi_tdata),
        .input_axis_tvalid(uart_tx_axi_tvalid),
        .input_axis_tready(uart_tx_axi_tready),
        // axi output
        .output_axis_tdata(uart_rx_axi_tdata),
        .output_axis_tvalid(uart_rx_axi_tvalid),
        .output_axis_tready(uart_rx_axi_tready),
        // uart
        .rxd(uart_rxd),
        .txd(uart_txd),
        // status
        .tx_busy(),
        .rx_busy(),
        .rx_overrun_error(),
        .rx_frame_error(),
        // configuration
        .prescale(52) // 115200 baud (calculate by clkf/(baud*8) = 48000000/(115200*8) )
    );

    reg [31:0] memory [0:MEM_SIZE-1];

    reg [31:0] m_read_data;
    reg m_read_en;

    always @(posedge clk) begin
        m_read_en <= 0;
        mem_ready <= mem_valid && !mem_ready && m_read_en;

        m_read_data <= memory[mem_addr >> 2];
        mem_rdata <= m_read_data;

        out_byte_en <= 0;
        out_byte <= out_byte;

        uart_rx_axi_tready <= 0;
        uart_tx_axi_tvalid <= 0;


        if (mem_valid && !mem_ready && !mem_wstrb && (mem_addr >> 2) < MEM_SIZE) 
        begin
            m_read_en <= 1;
        end
        if (mem_valid && !mem_ready && |mem_wstrb && (mem_addr >> 2) < MEM_SIZE)
        begin
            if (mem_wstrb[0]) memory[mem_addr >> 2][ 7: 0] <= mem_wdata[ 7: 0];
            if (mem_wstrb[1]) memory[mem_addr >> 2][15: 8] <= mem_wdata[15: 8];
            if (mem_wstrb[2]) memory[mem_addr >> 2][23:16] <= mem_wdata[23:16];
            if (mem_wstrb[3]) memory[mem_addr >> 2][31:24] <= mem_wdata[31:24];
            mem_ready <= 1;
        end
        if (mem_valid && !mem_ready && |mem_wstrb && mem_addr == 32'h1000_0000)
        begin
            out_byte_en <= 1;
            out_byte <= mem_wdata;
            mem_ready <= 1;
        end
        if (mem_valid && !mem_ready && mem_addr == 32'h2000_0000)
        begin
            if (|mem_wstrb)
            begin
                // write to TX
                mem_ready <= 1;
                uart_tx_axi_tdata <= mem_wdata;
                uart_tx_axi_tvalid <= 1;
            end
            else
            begin
                //read RX data
                mem_ready <= uart_rx_axi_tvalid;
                mem_rdata <= uart_rx_axi_tdata;
                uart_rx_axi_tready <= 1;
            end
        end

        if (mem_valid && !mem_ready && mem_addr == 32'h2000_0040)
        begin
                //read TX ready signal
                mem_ready <= 1;
                mem_rdata <= uart_rx_axi_tvalid;
        end


        if (mem_valid && !mem_ready && mem_addr == 32'h2000_0080)
        begin
                //read TX ready signal
                mem_ready <= 1;
                mem_rdata <= uart_tx_axi_tready;
        end

    end

endmodule
