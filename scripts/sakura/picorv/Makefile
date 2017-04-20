

TOOLCHAIN_PREFIX=/opt/riscv32i/bin/riscv32-unknown-elf-

all: firmware.hex

firmware.hex: firmware.S firmware.c firmware.lds
	$(TOOLCHAIN_PREFIX)gcc -Os -ffreestanding -nostdlib -o firmware.elf firmware.S firmware.c \
		 --std=gnu99 -Wl,-Bstatic,-T,firmware.lds,-Map,firmware.map,--strip-debug -lgcc
	$(TOOLCHAIN_PREFIX)objcopy -O binary firmware.elf firmware.bin
	python3 ../../../firmware/makehex.py firmware.bin 4096 > firmware.hex

clean:
	rm -f firmware.hex
