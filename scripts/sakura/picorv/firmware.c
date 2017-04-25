
// world's most simple boot loader



void putc(unsigned char c)
{
    *(volatile char*)0x20000000 = c;
}

unsigned char getc()
{
    return *(volatile char*)0x20000000;
}

void main()
{
    while(1)
    {
        putc(getc() + 1);
    }
}
