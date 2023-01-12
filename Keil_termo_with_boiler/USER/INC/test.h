//#include "stm32f0xx.h"
#include "user_init.h"

#define	value_led_1000		((uint16_t)0x0001)  // 0b00000001
#define	value_led_400			((uint16_t)0x0002)	//0b00000010
#define	value_led_40			((uint16_t)0x0003)	//0b00000011
#define	value_led_4				((uint16_t)0x0004)	//0b00000100
#define	value_led_400m		((uint16_t)0x0005)	//0b00000101
#define	value_led_40m			((uint16_t)0x0006)	//0b00000110

#define	value_led_1000_2		((uint16_t)0x000f)	//0b00001111
#define	value_led_400_2			((uint16_t)0x000e)	//0b00001110
#define	value_led_40_2			((uint16_t)0x000d)	//0b00001101
#define	value_led_4_2				((uint16_t)0x000b)	//0b00001011
#define	value_led_400m_2		((uint16_t)0x0007)	//0b00000111
#define	value_led_40m_2			((uint16_t)0x0008)	//0b00001000

typedef enum 
{
	led_1000,
	led_400,
	led_40,
	led_4,
	led_400m,
	led_40m
}range_test;

typedef enum {
	test1,
	test2,
	test3,
	test4,
	test5,
	test6
}TYPE_meas_led;

uint16_t mask_led (TYPE_meas_led m, uint16_t range_);
uint16_t select_range_led(TYPE_meas_led meas, range_test volt);
void Button_init(void);

