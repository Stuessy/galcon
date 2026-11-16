#include "SDL.h"

#define DEF32_ADD(srcp,dstp) \
    Sint16 tmp = *(dstp)+*(srcp); \
    *(dstp) = (tmp <= 255 ? tmp : 255);
    
#define DEF32_ADD_ALPHA(srcp,dstp) \
    Sint16 tmp = *(dstp)+((*(srcp)*alpha)>>8); \
    *(dstp) = (tmp <= 255 ? tmp : 255);

#define DEF32_SUBTRACT(srcp,dstp) \
    Sint16 tmp = *(dstp)-*(srcp); \
    *(dstp) = (tmp >= 0 ? tmp : 0);

#define DEF32_MULTIPLY(srcp,dstp) \
    *(dstp) = ((*(dstp))*(*(srcp)))>>8;

#define DEF32_MIN(srcp,dstp) \
    if (*(srcp) < *(dstp)) { *(dstp) = *(srcp); }

#define DEF32_MAX(srcp,dstp) \
    if (*(srcp) > *(dstp)) { *(dstp) = *(srcp); }
