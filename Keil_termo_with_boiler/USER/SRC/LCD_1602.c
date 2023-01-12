#include "LCD_1602.h"

//---Функция задержки---//
void delay(int a)
{
    int i = 0;
    int f = 0;
    while(f < a)
    {
        while(i<600)
            {i++;}
        f++;
    }
}
 
//---Нужная функция для работы с дисплеем, по сути "дергаем ножкой" EN---//
void PulseLCD()
{
    LCM_OUT_portA &= ~LCM_PIN_EN;
    delay(220);
    LCM_OUT_portA |= LCM_PIN_EN;
    delay(220);
    LCM_OUT_portA &= (~LCM_PIN_EN);
    delay(220);
}
 
//---Отсылка байта в дисплей---//
void SendByte(char ByteToSend, int IsData)
{
    LCM_OUT &= (~LCM_PIN_MASK); LCM_OUT_portA &= ~(LCM_PIN_MASK_portA);
    LCM_OUT |= ((ByteToSend & 0xF0) << 8);
 
    if (IsData == 1)
        LCM_OUT_portA |= LCM_PIN_RS;
    else
        LCM_OUT_portA &= ~LCM_PIN_RS;
    PulseLCD();
    LCM_OUT &= (~LCM_PIN_MASK); LCM_OUT_portA &= ~(LCM_PIN_MASK_portA);
    LCM_OUT |= ((ByteToSend & 0x0F) << 12);
 
    if (IsData == 1)
        LCM_OUT_portA |= LCM_PIN_RS;
    else
        LCM_OUT_portA &= ~LCM_PIN_RS;
 
    PulseLCD();
}
 
//---Установка позиции курсора---//
void Cursor(char Row, char Col)
{
   char address;
   if (Row == 0)
   address = 0;
   else
   address = 0x40;
   address |= Col;
   SendByte(0x80 | address, 0);
}
 
//---Очистка дисплея---//
void ClearLCDScreen()
{
    SendByte(0x01, 0);
    SendByte(0x02, 0);
}
 
//---Инициализация дисплея---//
void InitializeLCD(void)
{
	GPIO_InitTypeDef  GPIO_InitStructure;
	RCC -> AHBENR |= RCC_AHBPeriph_GPIOB;
	
    GPIO_InitStructure.GPIO_Pin = LCM_PIN_D4 | LCM_PIN_D5 | LCM_PIN_D6 | LCM_PIN_D7;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOB, &GPIO_InitStructure);
	
	RCC -> AHBENR |= RCC_AHBPeriph_GPIOA;
	
    GPIO_InitStructure.GPIO_Pin = LCM_PIN_RS | LCM_PIN_EN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOA, &GPIO_InitStructure);
	
    LCM_OUT &= ~(LCM_PIN_MASK); LCM_OUT_portA &= ~(LCM_PIN_MASK_portA);
    delay(32000);
    delay(32000);
    delay(32000);
    LCM_OUT_portA &= ~LCM_PIN_RS;
    LCM_OUT_portA &= ~LCM_PIN_EN;
    LCM_OUT = 0x20;
    PulseLCD();
    SendByte(0x28, 0);
    SendByte(0x0E, 0);
    SendByte(0x06, 0);
}
 
//---Печать строки---//
void PrintStr(char *Text)
{
    char *c;
    c = Text;
    while ((c != 0) && (*c != 0))
    {
        SendByte(*c, 1);
        c++;
    }
}
uint8_t func_acs2(double k);
extern uint8_t acs2_symb[8];

void out_temp_LCD(double tempA, double tempW){
	char dim_txt[10];
	char* txt_for_air;
	char* txt_for_wat;
		txt_for_air = "AIR ";

		txt_for_wat = "WATER ";

	size_t len_a = strlen(txt_for_air);
	size_t len_w = strlen(txt_for_wat);
	uint16_t t, m, k = 0;

	for(t = 0; t<len_a; t++){ dim_txt[k] = *txt_for_air; txt_for_air++; k++; }
	//////////////////////////////
	m = func_acs2(tempA);
	for(t = 0; t<m; t++){	dim_txt[k] = acs2_symb[t]; k++;	}
/////////////////////////////////
	dim_txt[k] = '\0';
/////////////////////////////////	
	ClearLCDScreen(); // очистка памяти дисплея
	Cursor(0,4); //Установка курсора, 0-ая строка, 4-ой столбец
	PrintStr(dim_txt);
	k = 0;
	
	for(t = 0; t<len_w; t++){ dim_txt[k] = *txt_for_wat; txt_for_wat++; k++; }
	//////////////////////////////
	m = func_acs2(tempW);
	for(t = 0; t<m; t++){	dim_txt[k] = acs2_symb[t]; k++;	}
/////////////////////////////////
	dim_txt[k] = '\0';
	Cursor(1,2);
	PrintStr(dim_txt);
	Cursor(1,16);
}
void out_alarm_LCD(char* alarm_txt){
	char dim_txt[32];
	char* alarm = "ALARM!";
	size_t len = strlen(alarm_txt);
	size_t lenA = strlen(alarm);
	uint16_t t, k = 0;
	
	for(t = 0; t<lenA; t++){ dim_txt[k] = *alarm; alarm++; k++; }
	dim_txt[k] = '\0';
	ClearLCDScreen(); // очистка памяти дисплея
	Cursor(0,5);
	PrintStr(dim_txt); k = 0;
	
	for(t = 0; t<len; t++){	dim_txt[k] = *alarm_txt; alarm_txt++; k++; }
	dim_txt[k] = '\0';
	Cursor(1,2);
	PrintStr(dim_txt);
	Cursor(1,16);
/////////////////////////////////
}
