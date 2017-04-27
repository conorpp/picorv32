
# RISC-V on SAKURA-G

Runs on main fpga.

UART pins RX and TX on Header pins 2 and 1, respectively.

Trigger is on Header pin 3.

## How to use

Install Xilinx ISE 14.7.

Run

```bash
make default    # build bitfile
make            # build firmware and update bitfile with firmware
make prog       # program FPGA.  Make sure programming cable is plugged in.
```


