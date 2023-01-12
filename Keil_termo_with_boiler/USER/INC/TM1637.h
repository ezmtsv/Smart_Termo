#ifndef __TM1637_H
#define __TM1637_H

#include "stm32f0xx.h"
#include <stdbool.h>
//#include "gpio.h"


#define	TM1637_DELAY_US				    4
#define	TM1637_BRIGHTNESS_MAX		  15

#define	TM1637_POSITION_MAX			  4

#define	TM1637_CMD_SET_DATA			  0x40
#define	TM1637_CMD_SET_ADDR			  0xC0
#define	TM1637_CMD_SET_DSIPLAY		0x80

#define	TM1637_SET_DATA_WRITE		  0x00 // write data to the display register
#define	TM1637_SET_DATA_READ		  0x02 // read the key scan data
#define	TM1637_SET_DATA_A_ADDR		0x00 // automatic address increment
#define	TM1637_SET_DATA_F_ADDR		0x04 // fixed address
#define	TM1637_SET_DATA_M_NORM		0x00 // normal mode
#define	TM1637_SET_DATA_M_TEST		0x10 // test mode
#define	TM1637_SET_DISPLAY_OFF		0x00 
#define	TM1637_SET_DISPLAY_ON		  0x08 
#define	TM1637_screen_minus			  64

#define TM1637_DAT_Pin            GPIO_Pin_4
#define TM1637_CLK_Pin            GPIO_Pin_3
#define TM1637_DAT_Pin_ON         (GPIOB->BSRR|= TM1637_DAT_Pin)
#define TM1637_DAT_Pin_OFF        (GPIOB->BRR|=	TM1637_DAT_Pin)
#define TM1637_CLK_Pin_ON         (GPIOB->BSRR|=TM1637_CLK_Pin)
#define TM1637_CLK_Pin_OFF        (GPIOB->BRR|=TM1637_CLK_Pin)
#define read_DAT_pin              ((GPIOB->IDR &	TM1637_DAT_Pin)>>4)	



void TM1637_init(void);
void TM1637_start(void);
void TM1637_stop(void);
unsigned char TM1637_write_byte(unsigned char value);
void TM1637_send_command(unsigned char value);
void TM1637_clear(void);
void TM1637_display_segments(unsigned char position, unsigned char segment_value, unsigned char colon_state);
void out_temp_LCDTM1637(double temp);
void disp_scrsave (uint8_t mode);
void disp_set_mode (uint8_t mode, uint8_t par);

#endif
