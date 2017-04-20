`timescale 1 ns / 1 ps

module system (
	input            clk,
	input            resetn,
	input            [1:0]M_PUSHSW,
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

	wire mem_la_read;
	wire mem_la_write;
	wire [31:0] mem_la_addr;
	wire [31:0] mem_la_wdata;
	wire [3:0] mem_la_wstrb;

    //wire [7:0] outb = (M_PUSHSW[0] == 1) ? (memory[mem_addr >> 2][7:0]) : out_byte;
    assign M_LED = {memory[mem_addr >> 2][1:0], out_byte};

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
        .mem_rdata   (mem_rdata   ),
        .mem_la_read (mem_la_read ),
        .mem_la_write(mem_la_write),
        .mem_la_addr (mem_la_addr ),
        .mem_la_wdata(mem_la_wdata),
        .mem_la_wstrb(mem_la_wstrb)
    );

	reg [31:0] memory [0:MEM_SIZE-1];
	initial $readmemh("firmware.hex", memory);

	reg [31:0] m_read_data;
	reg m_read_en;

	generate if (FAST_MEMORY) begin
		always @(posedge clk) begin
			mem_ready <= 1;
			out_byte_en <= 0;
			mem_rdata <= memory[mem_la_addr >> 2];
			if (mem_la_write && (mem_la_addr >> 2) < MEM_SIZE) begin
				if (mem_la_wstrb[0]) memory[mem_la_addr >> 2][ 7: 0] <= mem_la_wdata[ 7: 0];
				if (mem_la_wstrb[1]) memory[mem_la_addr >> 2][15: 8] <= mem_la_wdata[15: 8];
				if (mem_la_wstrb[2]) memory[mem_la_addr >> 2][23:16] <= mem_la_wdata[23:16];
				if (mem_la_wstrb[3]) memory[mem_la_addr >> 2][31:24] <= mem_la_wdata[31:24];
			end
			else
			if (mem_la_write && mem_la_addr == 32'h1000_0000) begin
				out_byte_en <= 1;
				out_byte <= mem_la_wdata;
			end
		end
    end else begin
        always @(posedge clk) begin
            m_read_en <= 0;
            mem_ready <= mem_valid && !mem_ready && m_read_en;

            m_read_data <= memory[mem_addr >> 2];
            mem_rdata <= m_read_data;

            out_byte_en <= 0;
            out_byte <= out_byte;

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
    end
    end endgenerate
endmodule
