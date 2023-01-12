#ifndef __USART_STM_H
#define __USART_STM_H

#include "stm32f0xx_usart.h"

typedef enum 
{
  COM1 = 0,
  COM2 = 1
} COM_TypeDef;  

/**
 * @brief Definition for COM port1, connected to USART1
 */ 
#define COMn                             2

#define EVAL_COM1                        USART1
#define EVAL_COM1_CLK                    RCC_APB2Periph_USART1

#define EVAL_COM1_TX_PIN                 GPIO_Pin_9
#define EVAL_COM1_TX_GPIO_PORT           GPIOA
#define EVAL_COM1_TX_GPIO_CLK            RCC_AHBPeriph_GPIOA
#define EVAL_COM1_TX_SOURCE              GPIO_PinSource9
#define EVAL_COM1_TX_AF                  GPIO_AF_1

#define EVAL_COM1_RX_PIN                 GPIO_Pin_10
#define EVAL_COM1_RX_GPIO_PORT           GPIOA
#define EVAL_COM1_RX_GPIO_CLK            RCC_AHBPeriph_GPIOA
#define EVAL_COM1_RX_SOURCE              GPIO_PinSource10
#define EVAL_COM1_RX_AF                  GPIO_AF_1

#define EVAL_COM1_CTS_PIN                GPIO_Pin_11
#define EVAL_COM1_CTS_GPIO_PORT          GPIOA
#define EVAL_COM1_CTS_GPIO_CLK           RCC_AHBPeriph_GPIOA
#define EVAL_COM1_CTS_SOURCE             GPIO_PinSource11
#define EVAL_COM1_CTS_AF                 GPIO_AF_1

#define EVAL_COM1_RTS_PIN                GPIO_Pin_12
#define EVAL_COM1_RTS_GPIO_PORT          GPIOA
#define EVAL_COM1_RTS_GPIO_CLK           RCC_AHBPeriph_GPIOA
#define EVAL_COM1_RTS_SOURCE             GPIO_PinSource12
#define EVAL_COM1_RTS_AF                 GPIO_AF_1
   
#define EVAL_COM1_IRQn                   USART1_IRQn

////////////////////////////

#define EVAL_COM2                        USART2
#define EVAL_COM2_CLK                    RCC_APB1Periph_USART2

#define EVAL_COM2_TX_PIN                 GPIO_Pin_2
#define EVAL_COM2_TX_GPIO_PORT           GPIOA
#define EVAL_COM2_TX_GPIO_CLK            RCC_AHBPeriph_GPIOA
#define EVAL_COM2_TX_SOURCE              GPIO_PinSource2
#define EVAL_COM2_TX_AF                  GPIO_AF_1

#define EVAL_COM2_RX_PIN                 GPIO_Pin_3
#define EVAL_COM2_RX_GPIO_PORT           GPIOA
#define EVAL_COM2_RX_GPIO_CLK            RCC_AHBPeriph_GPIOA
#define EVAL_COM2_RX_SOURCE              GPIO_PinSource3
#define EVAL_COM2_RX_AF                  GPIO_AF_1

#define EVAL_COM2_CTS_PIN                GPIO_Pin_0
#define EVAL_COM2_CTS_GPIO_PORT          GPIOA
#define EVAL_COM2_CTS_GPIO_CLK           RCC_AHBPeriph_GPIOA
#define EVAL_COM2_CTS_SOURCE             GPIO_PinSource0
#define EVAL_COM2_CTS_AF                 GPIO_AF_1

#define EVAL_COM2_RTS_PIN                GPIO_Pin_1
#define EVAL_COM2_RTS_GPIO_PORT          GPIOA
#define EVAL_COM2_RTS_GPIO_CLK           RCC_AHBPeriph_GPIOA
#define EVAL_COM2_RTS_SOURCE             GPIO_PinSource1
#define EVAL_COM2_RTS_AF                 GPIO_AF_1
   
#define EVAL_COM2_IRQn                   USART2_IRQn

#define SerialPutString(x)   Serial_PutString((uint8_t*)(x))
#define CMD_STRING_SIZE       128

void STM_EVAL_COMInit(COM_TypeDef COM, USART_InitTypeDef* USART_InitStruct);
void acs2(uint32_t k, uint8_t size_symb);
void SerialPutChar(uint8_t c);
void Serial_PutString(uint8_t *s);
void send_integer(uint32_t k);
void info_sendCOMPORT(void);
void comport_Init(COM_TypeDef COM, uint32_t baud);
void usart_init(void);

#endif


