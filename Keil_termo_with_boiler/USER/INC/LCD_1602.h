#include "stm32f0xx.h"
#include <string.h>
#include <stdbool.h>

//---Переопределяем порты для подключения дисплея, для удобства---//
#define     LCM_OUT               GPIOB->ODR
#define     LCM_OUT_portA         GPIOA->ODR
#define     LCM_PIN_RS            GPIO_Pin_9          // PA9
#define     LCM_PIN_EN            GPIO_Pin_10          // PA10
#define     LCM_PIN_D7            GPIO_Pin_15          // PB15
#define     LCM_PIN_D6            GPIO_Pin_14          // PB14
#define     LCM_PIN_D5            GPIO_Pin_13          // PB13
#define     LCM_PIN_D4            GPIO_Pin_12          // PB12
//#define     LCM_PIN_MASK  ((LCM_PIN_RS | LCM_PIN_EN | LCM_PIN_D7 | LCM_PIN_D6 | LCM_PIN_D5 | LCM_PIN_D4))
#define     LCM_PIN_MASK  ((LCM_PIN_D7 | LCM_PIN_D6 | LCM_PIN_D5 | LCM_PIN_D4))
#define     LCM_PIN_MASK_portA  ((LCM_PIN_RS | LCM_PIN_EN))

void SendByte(char ByteToSend, int IsData);
void Cursor(char Row, char Col);
void ClearLCDScreen();
void InitializeLCD(void);
void PrintStr(char *Text);
void out_temp_LCD(double tempA, double tempW);
void out_alarm_LCD(char* alarm_txt);

