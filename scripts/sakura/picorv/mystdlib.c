


void memmove(void * dst, const void * src, int size)
{
    int i;
    for (i = 0; i < size; i++)
    {
        ((unsigned char*)dst)[i] = ((unsigned char*)src)[i];
    }
}


void * memcpy(void * dst, const void * src, int n)
{
    memmove(dst, src, n);
    return dst;
}


void * memset(void * dst, int c, int n)
{
    int i;
    for (i = 0; i < n; i++)
    {
        ((unsigned char*)dst)[i] = c;
    }
    return dst;
}


int memcmp(unsigned char  * dst, unsigned char * src, int n)
{
    int i;
    for (i = 0; i < n; i++)
    {
        if(dst[i] != src[i])
            return -1;
    }
    return 0;
}
