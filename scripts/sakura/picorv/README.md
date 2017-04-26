
# RISC-V on SAKURA-G

Runs on main fpga.

UART pins RX/TX on Header pins 2/1, respectively.

## How to use

Install Xilinx ISE 14.7.

Run

```bash
make default    # build bitfile
make            # build firmware and update bitfile with firmware
make prog       # program FPGA
```


