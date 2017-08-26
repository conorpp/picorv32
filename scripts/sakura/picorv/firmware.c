#include <stdint.h>
#include <string.h>
#include "aes.h"
//#include <stdio.h>

void putc(unsigned char c)
{
    while((*(volatile char *)0x20000080) == 0)
    {}
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

void puts(unsigned char * s)
{
    while(*s)
        putc(*s++);
}



static void test_encrypt_ecb(void)
{
    uint8_t key[] = {0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c};
    uint8_t in[]  = {0x6b, 0xc1, 0xbe, 0xe2, 0x2e, 0x40, 0x9f, 0x96, 0xe9, 0x3d, 0x7e, 0x11, 0x73, 0x93, 0x17, 0x2a};
    uint8_t out[] = {0x3a, 0xd7, 0x7b, 0xb4, 0x0d, 0x7a, 0x36, 0x60, 0xa8, 0x9e, 0xca, 0xf3, 0x24, 0x66, 0xef, 0x97};
    uint8_t buffer[16];
    //uint8_t debug[16] = {0x11, 0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11}; //Yuan, add debug message

    AES128_ECB_encrypt(in, key, buffer, 1, 1);
    AES128_ECB_encrypt(in, key, buffer, 1, 0);

    /*puts("ECB encrypt: ");*/

    if(0 == memcmp((char*) out, (char*) buffer, 16))
    {
        /*puts("SUCCESS!\r\n");*/
    }
    else
    {
        puts("FAILURE!\r\n");
    }
}

void echo(int i)
{
    putc(getc() + i);
}

#define CMD_PT          0
#define CMD_KEY         1
#define CMD_RUN         2
#define CMD_CT          3
#define CMD_OKAY        4
#define CMD_SET_PLAIN   5
#define CMD_SET_MASKED  6
#define CMD_PRINT       7 // Yuan: debug
#define CMD_ERROR       'A'
#define CMD_debug       'D'

#define TRIGGER (*(volatile uint32_t*)0x200000a0)

void main()
{
    uint8_t msg[17];
    uint8_t reply[17];
    uint8_t pt[16];
    uint8_t key[16];
    uint8_t ct[16];
    uint8_t rng[16];
    uint8_t key2[16];

    uint8_t debug[16] = {0x11, 0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11};

   // uint8_t debug[16]; //Yuan: debug for output message

    uint8_t masked = 0;

    uint8_t ret = CMD_ERROR;

    /*while(1) echo(masked++);*/
    //while(1) puts("hello world\r\n");

    TRIGGER = 0;
    test_encrypt_ecb();
    memset(rng,0,sizeof(rng));

    int i, k;

    while(1)
    {
        // get RNG here

        //
        get_message(msg);
        memmove(key, key2, 16);

        switch(msg[0])
        {
            case CMD_PT:
                memmove(pt, msg + 1, 16);
                ret = CMD_OKAY;
                break;
            case CMD_KEY:
                memmove(key, msg + 1, 16);
                memmove(key2, msg + 1, 16);
                memmove(reply+1, msg + 1, 16);

                // runs key expansion and returns
                AES128_ECB_encrypt(pt, key, ct, masked, 1);

                ret = CMD_OKAY;
                break;
             // Yuan: add this Output message print for instruction skip experiment
            case CMD_PRINT: 

                //TRIGGER = 0xffffffff;
                memmove(reply + 1, debug, 16);
                ret = CMD_debug;
                break;

            case CMD_RUN:
                // run
                if (1)
                {
                    RNG = rng;
                    //TRIGGER = 0xffffffff; 

                    TRIGGER = 0;
                    TRIGGER = 0xffffffff;
                    TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;

                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;

                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;




                    for (i = 0; i < 16; i++)
                    {
                        //i = 0;
                        pt[i] ^= rng[i];
                    }

                    /*asm("inf:");*/
                    /*asm("j inf");*/
                    //put_message(debug);

                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;

                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;


                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;
                    // TRIGGER = 0;
                    // TRIGGER = 0xffffffff;

                    /*AES128_ECB_encryptm(pt, key, ct, masked, 0);*/
                    AES128_ECB_encrypt(pt, key, ct, masked, 0);
                    memmove(reply + 1, ct, 16);
                    //-----------------------------------------------------------/
                    // Yuan : add this marker to find the rng generator position
                    //-----------------------------------------------------------/
                    TRIGGER = 0;
                    TRIGGER = 0xffffffff;
                    TRIGGER = 0;
                    TRIGGER = 0xffffffff;
                    TRIGGER = 0;
                    TRIGGER = 0xffffffff;
                    TRIGGER = 0;
                    
                    memmove(rng, ct, 16);

                    TRIGGER = 0;
                    TRIGGER = 0xffffffff;
                    TRIGGER = 0;
                    TRIGGER = 0xffffffff;
                    TRIGGER = 0;
                    TRIGGER = 0xffffffff;
                    TRIGGER = 0;

                    TRIGGER = 0;
                    ret = CMD_CT;

                }
     
 
                ret = CMD_CT;

                break;
            case CMD_SET_PLAIN:
                masked = 0;
                ret = CMD_OKAY;
                break;
            case CMD_SET_MASKED:
                masked = 1;
                ret = CMD_OKAY;
                break;
            default:
                ret = CMD_ERROR;
        }

        reply[0] = ret;
        put_message(reply);

    }
}
