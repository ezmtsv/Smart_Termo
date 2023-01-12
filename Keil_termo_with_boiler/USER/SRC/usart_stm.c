#include "usart_stm.h"
#include <string.h>

USART_TypeDef* comport_USART = EVAL_COM2;

USART_TypeDef* COM_USART[COMn] = {EVAL_COM1, EVAL_COM2}; 

GPIO_TypeDef* COM_TX_PORT[COMn] = {EVAL_COM1_TX_GPIO_PORT, EVAL_COM2_TX_GPIO_PORT};
 
GPIO_TypeDef* COM_RX_PORT[COMn] = {EVAL_COM1_RX_GPIO_PORT, EVAL_COM2_RX_GPIO_PORT};

const uint32_t COM_USART_CLK[COMn] = {EVAL_COM1_CLK, EVAL_COM2_CLK};

const uint32_t COM_TX_PORT_CLK[COMn] = {EVAL_COM1_TX_GPIO_CLK, EVAL_COM2_TX_GPIO_CLK};
 
const uint32_t COM_RX_PORT_CLK[COMn] = {EVAL_COM1_RX_GPIO_CLK, EVAL_COM2_RX_GPIO_CLK};

const uint16_t COM_TX_PIN[COMn] = {EVAL_COM1_TX_PIN, EVAL_COM2_TX_PIN};

const uint16_t COM_RX_PIN[COMn] = {EVAL_COM1_RX_PIN, EVAL_COM2_RX_PIN};
 
const uint16_t COM_TX_PIN_SOURCE[COMn] = {EVAL_COM1_TX_SOURCE, EVAL_COM2_TX_SOURCE};

const uint16_t COM_RX_PIN_SOURCE[COMn] = {EVAL_COM1_RX_SOURCE, EVAL_COM2_RX_SOURCE};
 
const uint16_t COM_TX_AF[COMn] = {EVAL_COM1_TX_AF, EVAL_COM2_TX_AF};
 
const uint16_t COM_RX_AF[COMn] = {EVAL_COM1_RX_AF, EVAL_COM2_RX_AF};

unsigned char acs2_data[20];
///////////////////////////////////
uint16_t len_packet_COM;
///////////////////////////////////

void STM_EVAL_COMInit(COM_TypeDef COM, USART_InitTypeDef* USART_InitStruct)
{
  GPIO_InitTypeDef GPIO_InitStructure;

  /* Enable GPIO clock */
  RCC_AHBPeriphClockCmd(COM_TX_PORT_CLK[COM] | COM_RX_PORT_CLK[COM], ENABLE);

  /* Enable USART clock */
//  RCC_APB2PeriphClockCmd(COM_USART_CLK[COM], ENABLE); 
	if(COM == COM1){
		RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE); 	//Включаем тактирование порта USART1
	}
	if(COM == COM2){
		RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, ENABLE); 	//Включаем тактирование порта USART2
	}
	
  /* Connect PXx to USARTx_Tx */
  GPIO_PinAFConfig(COM_TX_PORT[COM], COM_TX_PIN_SOURCE[COM], COM_TX_AF[COM]);

  /* Connect PXx to USARTx_Rx */
  GPIO_PinAFConfig(COM_RX_PORT[COM], COM_RX_PIN_SOURCE[COM], COM_RX_AF[COM]);
  
  /* Configure USART Tx as alternate function push-pull */
  GPIO_InitStructure.GPIO_Pin = COM_TX_PIN[COM];
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
  GPIO_Init(COM_TX_PORT[COM], &GPIO_InitStructure);
    
  /* Configure USART Rx as alternate function push-pull */
  GPIO_InitStructure.GPIO_Pin = COM_RX_PIN[COM];
  GPIO_Init(COM_RX_PORT[COM], &GPIO_InitStructure);

  /* USART configuration */
  USART_Init(COM_USART[COM], USART_InitStruct);
    
  /* Enable USART */
  USART_Cmd(COM_USART[COM], ENABLE);
}

void acs2(uint32_t k, uint8_t size_symb)			// size_symb - кол-во символов числа , k - число		
{
	unsigned char t; 
	uint32_t g;
	for(t = size_symb; t>0; t--) {
		g = k % 10;
		k = k / 10;
		acs2_data[t-1] = g + '0';
	}
	acs2_data[size_symb] = '\0';
}

void SerialPutChar(uint8_t c)
{ 
	USART_SendData(comport_USART, c);
  while (USART_GetFlagStatus(comport_USART, USART_FLAG_TXE) == RESET)
  {}

}

/**
  * @brief  Print a string on the HyperTerminal
  * @param  s: The string to be printed
  * @retval None
  */
void Serial_PutString(uint8_t *s)
{
  while (*s != '\0')
  {
    SerialPutChar(*s);
    s++;
  }
}
////////////////////////////////////

/////////////////////////////////////
void send_integer(uint32_t k){
	uint32_t size = 0;
	uint32_t rez = k;
	while(rez != 0){
		rez/=10;
		size++;
	}
				acs2(k, size);
				SerialPutString(acs2_data);
				memset(acs2_data,0, sizeof(acs2_data));
}
void info_sendCOMPORT(void){
  SerialPutString("\r\n======================================================================");
  SerialPutString("\r\n=              (C) COPYRIGHT 2018 APstudio_EZ                        =");
  SerialPutString("\r\n=                                                                    =");
  SerialPutString("\r\n=  STM32F0xx In-Application Programming Application  (Version 1.0.0) =");
  SerialPutString("\r\n=                                                                    =");
  SerialPutString("\r\n=                                   By MCD Application Team          =");
  SerialPutString("\r\n======================================================================");
  SerialPutString("\r\n\r\n");

}

void comport_Init(COM_TypeDef COM, uint32_t baud)
{
	USART_InitTypeDef USART_InitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;
  /* USART resources configuration (Clock, GPIO pins and USART registers) ----*/
  /* USART configured as follow:
        - BaudRate = 115200 baud  
        - Word Length = 8 Bits
        - One Stop Bit
        - No parity
        - Hardware flow control disabled (RTS and CTS signals)
        - Receive and transmit enabled
  */
	
  USART_InitStructure.USART_BaudRate = baud;
  USART_InitStructure.USART_WordLength = USART_WordLength_8b;
  USART_InitStructure.USART_StopBits = USART_StopBits_1;
  USART_InitStructure.USART_Parity = USART_Parity_No;
  USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
  USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
  STM_EVAL_COMInit(COM, &USART_InitStructure);
		
	//	  NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
	////// ЕСЛИ прерывания инициализировать раньше в коде, то не работает
	  NVIC_InitStructure.NVIC_IRQChannelPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
		if(COM == COM1){
			NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
			NVIC_Init(&NVIC_InitStructure);
			USART1->CR1 	|= USART_CR1_RXNEIE;	// USART1 ON, TX ON, RX ON
			NVIC_EnableIRQ (USART1_IRQn);
		}
		if(COM == COM2){
			NVIC_InitStructure.NVIC_IRQChannel = USART2_IRQn;
			NVIC_Init(&NVIC_InitStructure);
			USART2->CR1 	|= USART_CR1_RXNEIE;;	// USART1 ON, TX ON, RX ON
			NVIC_EnableIRQ (USART2_IRQn);
		}				
//	USART1->CR1 	|= USART_CR1_UE | USART_CR1_TE | USART_CR1_RE |	USART_CR1_RXNEIE;	// USART1 ON, TX ON, RX ON
//	NVIC_EnableIRQ (USART1_IRQn);
	
//	info_sendCOMPORT(); 
}

/////////////////////////////////
void usart_init(void)
{
  GPIO_InitTypeDef GPIO_InitStructure; //Структура содержащая настройки порта
  USART_InitTypeDef USART_InitStructure; //Структура содержащая настройки USART
 
  RCC_AHBPeriphClockCmd(RCC_AHBPeriph_GPIOA, ENABLE); 		//Включаем тактирование порта A
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, ENABLE); 	//Включаем тактирование порта USART2
 
  GPIO_PinAFConfig(GPIOA, GPIO_PinSource3, GPIO_AF_1); //Подключаем PA3 к TX USART2				GPIO_AF_USART2
  GPIO_PinAFConfig(GPIOA, GPIO_PinSource2, GPIO_AF_1); //Подключаем PA2 к RX USART2  GPIO_AF_USART2
 
  //Конфигурируем PA2 как альтернативную функцию -> TX UART. Подробнее об конфигурации можно почитать во втором уроке.
  GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
 
  //Конфигурируем PA3 как альтернативную функцию -> RX UART. Подробнее об конфигурации можно почитать во втором уроке.
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
 
  USART_InitStructure.USART_BaudRate = 115200;
  USART_InitStructure.USART_WordLength = USART_WordLength_8b;
  USART_InitStructure.USART_StopBits = USART_StopBits_1;
  USART_InitStructure.USART_Parity = USART_Parity_No;
  USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
  USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
	USART_Init(USART2, &USART_InitStructure);
//  USART_StructInit(&USART_InitStructure); //Инициализируем UART с дефолтными настройками: скорость 9600, 8 бит данных, 1 стоп бит
 
  USART_Init(USART2, &USART_InitStructure);
  USART_Cmd(USART2, ENABLE); //Включаем UART
	
//	info_sendCOMPORT(); 
}
////////////////////////////////////
