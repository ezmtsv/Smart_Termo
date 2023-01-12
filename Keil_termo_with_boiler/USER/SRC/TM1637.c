#include "TM1637.h"
//Рус//

void DelayuS (__IO uint32_t nCount);
void DelaymS (__IO uint32_t nCount);

bool last_temp_minus = false;

const unsigned char seg_data[10] =
{
	0x3F, // 0
	0x06, // 1
	0x5B, // 2
	0x4F, // 3
	0x66, // 4
	0x6D, // 5
	0x7D, // 6
	0x07, // 7
	0x7F, // 8
	0x6F  // 9
};
const unsigned char air_segm[] =
{
	0x7F, // "В"
	0x3F, // "O"
	0x4F, // "З"
  0,
};
const unsigned char heat_segm[] =
{
	0x76, // "Н"
	0x77, // "А"
	0x31, // "Г"
  0,
};
const unsigned char street_segm[] =
{
	0x37, // "П"
	0x3F, // "О"
	0x31, // "Г"
  0,
};
const unsigned char boiler_segm[] =
{
	0x31, // "Г"
	0x3F, // "О"
	0x73, // "Р"
  0,
};



void TM1637_init(void) {
  GPIO_InitTypeDef  GPIO_InitStructure;
  RCC -> AHBENR |= RCC_AHBPeriph_GPIOB;
  GPIO_InitStructure.GPIO_Pin = TM1637_DAT_Pin | TM1637_CLK_Pin;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
//  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOB, &GPIO_InitStructure);	
    
	TM1637_send_command (TM1637_CMD_SET_DSIPLAY | TM1637_BRIGHTNESS_MAX | TM1637_SET_DISPLAY_ON);
	TM1637_clear();
}



void TM1637_start (void) {
	TM1637_DAT_Pin_ON;
	TM1637_CLK_Pin_ON;
	DelayuS (4);
	TM1637_DAT_Pin_OFF;
}



void TM1637_stop(void) {
	TM1637_CLK_Pin_OFF;
	DelayuS (4);
	TM1637_DAT_Pin_OFF;
	DelayuS (4);

	TM1637_CLK_Pin_ON;
	DelayuS (4);

	TM1637_DAT_Pin_ON;
}



unsigned char TM1637_write_byte (unsigned char value) {
	unsigned char i = 0x08;
	unsigned char ack = 0;
	GPIO_InitTypeDef GPIO_InitStruct;
	while (i)	{
		TM1637_CLK_Pin_OFF;
		DelayuS (4);
		if (value & 0x01)	{
			TM1637_DAT_Pin_ON;
		} else {
			TM1637_DAT_Pin_OFF;
		}
		TM1637_CLK_Pin_ON;
		DelayuS (4);
		value >>= 1;
		i--;
	}
	TM1637_CLK_Pin_OFF;
	GPIO_InitStruct.GPIO_Pin = TM1637_DAT_Pin;
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_Init (GPIOA, &GPIO_InitStruct);

	DelayuS (4);

	ack = read_DAT_pin;
	
	GPIO_InitStruct.GPIO_Pin = TM1637_DAT_Pin;
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_OUT;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_Level_3;
	GPIO_Init (GPIOA, &GPIO_InitStruct);

	if (ack) {
		TM1637_DAT_Pin_OFF;
	}
	DelayuS (4);
	TM1637_CLK_Pin_ON;
	DelayuS (4);
	TM1637_CLK_Pin_OFF;
	DelayuS (4);
	return (ack);
}



void TM1637_send_command (unsigned char value) {
	TM1637_start();
	TM1637_write_byte (value);
	TM1637_stop();
}



void TM1637_clear(void) {
	signed char i = (TM1637_POSITION_MAX - 1);
	while (i > -1) {
		TM1637_display_segments (i, 0x00, 0x00);
		i--;
	};
}



void TM1637_display_segments (unsigned char position, unsigned char segment_value, unsigned char colon_state) {
/*	
	if(position == 1)
	{
		switch(colon_state)
		{
			case 1:
			{
				segment_value |= 0x80;
				break;
			}
			default:
			{
				segment_value &= 0x7F;
				break;
			}
		}
	}
*/
  switch (colon_state) {
  case 1:
		segment_value |= 0x80;
		break;
	default:
		segment_value &= 0x7F;
    break;

  }
	TM1637_send_command (TM1637_CMD_SET_DATA | TM1637_SET_DATA_F_ADDR);
	TM1637_start();
	TM1637_write_byte (TM1637_CMD_SET_ADDR | (position & (TM1637_POSITION_MAX - 1)));
	TM1637_write_byte (segment_value);
	TM1637_stop();
}

//uint8_t func_acs2(double k);
//uint8_t acs2_symb[8];

void out_temp_LCDTM1637 (double temp) {
	uint8_t i;
//	uint8_t cont_dig;
	uint16_t temp_int;
//	TM1637_clear();

	if (temp<0) { 
		TM1637_display_segments(0, TM1637_screen_minus, 0); 					/// write minus in 0 znak/place
		temp = temp*(-1); 
		last_temp_minus = true; 
	} else {
		if (last_temp_minus) { TM1637_clear(); last_temp_minus = false; }
	}
	temp_int = (uint16_t)(temp*10);
	i = temp_int%10; TM1637_display_segments (3, seg_data[i], 0);
	temp_int = temp_int/10; i = temp_int%10; TM1637_display_segments (2, seg_data[i], 1);
	temp_int = temp_int/10; i = temp_int%10; TM1637_display_segments (1, seg_data[i], 0);
}



void disp_scrsave (uint8_t mode) {
  unsigned char *dim;      
  switch (mode) {
  case 1:
    dim = (unsigned char*)&air_segm;   
    break;
  case 2:
    dim = (unsigned char*)&heat_segm;
    break;
  case 3:
    dim = (unsigned char*)&street_segm;
    break;
  case 4:
    dim = (unsigned char*)&boiler_segm;
    break;
  } 
  for (uint8_t i=0; i<4; ++i) TM1637_display_segments(i, dim[i], 0);      
  TM1637_display_segments(2, dim[2], 1);
  DelaymS (1000);
  TM1637_display_segments(0, 0, 0);
  for (uint8_t i=1; i<4; ++i) TM1637_display_segments(i, dim[i-1], 0);      
  TM1637_display_segments(3, dim[2], 1);
  DelaymS (1000);
}



void disp_set_mode (uint8_t mode, uint8_t par) {
  uint8_t seg[4];
  seg[2] = seg_data[par/10];  seg[3] = seg_data[par%10];
  switch (mode) {
    case 1:
      seg[0] = 0x77;  seg[1] = 0;           // 0x77 - "A" air
      break;
    case 2:
      seg[0] = 0x3D;  seg[1] = 0x77;        // 0x3D - "G", 0x77 - "A" gisteresis for Air
      break;
    case 3:
      seg[0] = 0x7C;  seg[1] = 0;           // 0x7C - "b" boiler
      break;    
    case 4:
      seg[0] = 0x3D;  seg[1] = 0x7C;        // 0x3D - "G", 0x7C - "b" gisteresis for boiler
      break;
    case 5:
      seg[0] = 0x3F;  seg[1] = 0x71;        // 0x3F - "O", 0x71 - "F" output func
      seg[2] = 0;  
      break;
    case 6:
      seg[0] = 0x39;  seg[1] = 0x38;        // 0x39 - "C", 0x38 - "L" cycle for out display
      seg[2] = 0;  
      break; 
    case 7:
      seg[0] = 0x7C;  seg[1] = 0;        // 0x7C - "b" boiler
      if (par == 1) {
        seg[2] = 0x5C;                   //  0x2C - "o"
        seg[3] = 0x54;                   //  0x54 - "n"  
      } else {
        seg[2] = 0x5C;                   //  0x2C - "o"
        seg[3] = 0xF1;                   //  0xF1 - "F"
      }
      break;  
    case 8:
        seg[0] = 0x76;                    // 0x76 "Н"
        if (par == 0x33) seg[1] = seg_data[1];
        if (par == 0x55) seg[1] = seg_data[2];
        seg[2] = 0x5C;                   //  0x2C - "o"
        seg[3] = 0x54;                   //  0x54 - "n"  
      break;
  }
  for (uint8_t i=0; i<4; ++i) TM1637_display_segments(i, seg[i], 0); 
}
/***  bits
     0
     ͞ 
  5|   |1
     _
     6
  4|   |2
     3
     ͞ 
***/
