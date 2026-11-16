#ifndef DEF32_H
#define DEF32_H
#line 1 "def32.pyp"
#include "SDL.h"
#line 3 "def32.pyp"
#define DEF32_ADD(srcp,dstp)      Sint16 tmp = *(dstp)+*(srcp);      *(dstp) = (tmp <= 255 ? tmp : 255);
#line 7 "def32.pyp"
#define DEF32_ADD_ALPHA(srcp,dstp)      Sint16 tmp = *(dstp)+((*(srcp)*alpha)>>8);      *(dstp) = (tmp <= 255 ? tmp : 255);
#line 11 "def32.pyp"
#define DEF32_SUBTRACT(srcp,dstp)      Sint16 tmp = *(dstp)-*(srcp);      *(dstp) = (tmp >= 0 ? tmp : 0);
#line 15 "def32.pyp"
#define DEF32_MULTIPLY(srcp,dstp)      *(dstp) = ((*(dstp))*(*(srcp)))>>8;
#line 18 "def32.pyp"
#define DEF32_MIN(srcp,dstp)      if (*(srcp) < *(dstp)) { *(dstp) = *(srcp); }
#line 21 "def32.pyp"
#define DEF32_MAX(srcp,dstp)      if (*(srcp) > *(dstp)) { *(dstp) = *(srcp); }
#endif
