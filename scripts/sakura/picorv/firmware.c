#include <stdint.h>

void memmove(uint8_t * dst, uint8_t * src, int size)
{
    int i;
    for (i = 0; i < size; i++)
    {
        dst[i] = src[i];
    }
}

void putc(unsigned char c)
{
    *(volatile char*)0x20000000 = c;
}

unsigned char getc()
{
    return *(volatile char*)0x20000000;
}

void get_message(uint8_t * dst)
{
    int i;
    for (i = 0; i < 17; i++)
    {
        dst[i] = getc();
    }
}


void put_message(uint8_t * src)
{
    int i;
    for (i = 0; i < 17; i++)
    {
        putc(src[i]);
    }
}


#define CMD_PT      0
#define CMD_KEY     1
#define CMD_RUN     2
#define CMD_CT      3
#define CMD_OKAY    4
#define CMD_ERROR   'A'

void main()
{
    uint8_t msg[17];
    uint8_t reply[17];
    uint8_t pt[16];
    uint8_t key[16];
    uint8_t ct[16];

    uint8_t ret;

    while(1)
    {
        get_message(msg);

        switch(msg[0])
        {
            case CMD_PT:
                memmove(pt, msg + 1, 16);
                ret = CMD_OKAY;
                break;
            case CMD_KEY:
                memmove(key, msg + 1, 16);
                ret = CMD_OKAY;
                break;
            case CMD_RUN:
                // run
                ret = CMD_CT;
                break;
            default:
                ret = CMD_ERROR;
        }

        reply[0] = ret;
        put_message(reply);

    }
}
