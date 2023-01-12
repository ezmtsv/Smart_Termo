/**
  ******************************************************************************
  * @file    USB_Example/stm32f0xx_it.c 
  * @author  MCD Application Team
  * @version V1.0.0
  * @date    17-January-2014
  * @brief   Main Interrupt Service Routines.
  *          This file provides template for all exceptions handler and 
  *          peripherals interrupt service routine.
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2014 STMicroelectronics</center></h2>
  *
  * Licensed under MCD-ST Liberty SW License Agreement V2, (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  *        http://www.st.com/software_license_agreement_liberty_v2
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************
  */ 

/* Includes ------------------------------------------------------------------*/
#include "stm32f0xx_it.h"
#include "TM1637.h"
#include <string.h>
/** @addtogroup STM32F072B_DISCOVERY
  * @{
  */

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
//extern __IO uint32_t Gv_EOA;
extern USB_CORE_HANDLE USB_Device_dev;
extern uint8_t Send_Buffer[64];  //??extern uint8_t Send_Buffer[135];//65
extern uint8_t Report_buf[64];
extern uint8_t PrevXferDone;

uint16_t count_work_heat = 0;
uint16_t count_off_power = 0;
uint16_t cnt_LED = 0;
uint8_t count_minute = 0;
uint8_t count_ready_RST_DEF_ESP = 0;
uint8_t end_time_wait = 3;
uint8_t count_repeat = 0;
uint8_t status_heat_var;
uint8_t status_power220_var;

bool flag_ready_RST_ESP = false;
bool flag_ever_hour = false;
bool start_flag_ever_hour = false;
uint16_t cnt_set_mode = 0;
bool flag_set_mode = false;


extern buffer_OUT_SMART out_for_SMART;
extern uint8_t flag_change_TMP;

int seconds_time = 0;
int seconds_start_server = 0;
work_param PARAM;

unsigned int time_DAY;			
unsigned int time_NIGHT;
uint16_t ll = 0;
/////////////////////////////////////////
uint8_t count_answer_com = 0;							// секундный счетчик перерывов между приёмом байтов от ком порта 
unsigned char in_comPORT[300];
unsigned char in_comEND[2];
uint32_t	countIN_com = 0;
uint8_t	count_comEND = 0; 
uint16_t	stop_dog = 0;
uint8_t	count_restart = 0;

uint8_t mode_disp = 3;
uint8_t set_mode = 8;
uint16_t cnt_for_outDISP = 0;

//////////////////////////////////
extern bool flag_OK_comWIFI;
extern unsigned char acs2_data[20];
extern bool flag_start_get_dataCOM;
extern bool flag_initESP8266;
extern bool end_timeout_getdata;
extern struct param_ESP8266 init_param;
extern uint8_t data_OUT_COM[192];
extern uint8_t all_number_connect_WIFI;
extern bool flag_start_sendWIFI;
extern uint16_t len_packet_COM;
extern bool modul_ESP_connect;
extern uint16_t count_modul_disconnect;
extern bool start_initESP;
extern bool debug_Transmit_USB;
extern uint8_t buf_data[64];
extern bool flag_send_MAIL;
extern uint8_t flag_report_mail_debug;
extern bool flag_COOL;


void data_answer (void);	
void recieved_word (unsigned char RXc, uint16_t count_in);
void DelaymS (__IO uint32_t nCount);
void DelayuS (__IO uint32_t nCount);
uint32_t flash_read (uint32_t address);
void reset_ESP8266 (void);



void NMI_Handler (void) {

}



void HardFault_Handler (void) {
  /* Go to infinite loop when Hard Fault exception occurs */
  while (1) {
    LED_R_toggle;
    LED_G_toggle;
    DelaymS(200);
  }
}



void SVC_Handler (void) {
  
}



void PendSV_Handler (void) {
  
}



void SysTick_Handler (void) {
  
}



/******************************************************************************/
/*                 STM32F0xx Peripherals Interrupt Handlers                   */
/*  Add here the Interrupt Handler for the used peripheral(s) (PPP), for the  */
/*  available peripheral interrupt handler's name please refer to the startup */
/*  file (startup_stm32f0xx.s).                                            */
/******************************************************************************/


void USB_IRQHandler (void) {
  USB_Istr();
}



void USART2_IRQHandler (void) {
  uint8_t  RXc; 
  if (!flag_start_get_dataCOM) {									        // начало приема данных, выставляем флаг flag_start_get_dataCOM
    flag_start_get_dataCOM = true;
  };
  count_answer_com = 0;									                  // с каждым принятым байтом обнуляем счетчик паузы между байтами
  if (USART2->CR1 & USART_FLAG_RXNE) {
    RXc = USART_ReceiveData(USART2);
    in_comPORT[countIN_com] = RXc;
    recieved_word(RXc, countIN_com);											// отслеживаем приход служебных слов
    countIN_com++;
    // отслеживаем приход ОК
    in_comEND[count_comEND] = RXc; 
    count_comEND++;
    if (count_comEND == 2) {
      if (in_comEND[0] == 'O' && in_comEND[1] == 'K') {		//конец приема пакета
			  flag_OK_comWIFI = true;														// ОК получен, выставляем флаг flag_OK_comWIFI
				count_comEND = 0;                                 // ОК получен, сбрасываем счетчик полученных байт в ноль
																
      } else {
        in_comEND[0] = in_comEND[1];
        count_comEND = 1;
      }
    }
  }
	if (countIN_com>400) countIN_com = 0;	                  //400
}



void EXTI0_1_IRQHandler (void)
{ 
  /*
  if (EXTI->PR & EXTI_Line1) {
    if (GPIOB->IDR & STATUS_220) {						      // проверка, выставлен ли бит ADC_IN9
    // status_power220_var = status_NO;
    out_for_SMART.status_power220 = status_NO;
    }	else {
      // status_power220_var = status_OK;
			out_for_SMART.status_power220 = status_OK;
		}
	}
	DelaymS (300);*/
  EXTI_ClearITPendingBit (EXTI_Line0 | EXTI_Line1);
}



void EXTI4_15_IRQHandler(void) {                    // прерывания от кнопки
  if (EXTI->PR & EXTI_Line8) {
    if (cnt_LED % 2) {
      led_one_ON (LED_G);
      led_one_OFF (LED_R);
    }	else {
			led_one_OFF(LED_G);
		}
    cnt_LED++;
    if (cnt_LED>5) cnt_LED = 0;		
		count_restart++;
  }
  if (EXTI->PR & EXTI_Line9) {
    if (GPIOB->IDR & STATUS_220) {						      // проверка, выставлен ли бит ADC_IN9
    // status_power220_var = status_NO;
    out_for_SMART.status_power220 = status_NO;
    }	else {
      // status_power220_var = status_OK;
			out_for_SMART.status_power220 = status_OK;
		}
	}
  DelaymS(70);
	EXTI_ClearITPendingBit(EXTI_Line8);
  EXTI_ClearITPendingBit(EXTI_Line9);
  // EXTI_ClearITPendingBit(EXTI_Line4 | EXTI_Line15);
}



void read_FLASH_CORR (void);
//  void get_cor_coeff (void);
//  extern int debag_tmp;
/*
uint16_t cnt_out_disp = 0;
void TIM16_IRQHandler (void) {
  if (cnt_out_disp < 15) out_temp_LCDTM1637 (((double)out_for_SMART.tmp_AIR)/100);
  else if (cnt_out_disp < 30) out_temp_LCDTM1637 (((double)out_for_SMART.tmp_WATER)/100);
  else if (cnt_out_disp < 45) out_temp_LCDTM1637 (((double)out_for_SMART.tmp_Boiler)/100);
  else if (cnt_out_disp < 60) out_temp_LCDTM1637 (((double)out_for_SMART.tmp_Outdoor)/100);
  else cnt_out_disp = 0;
  
  cnt_out_disp++;
  TIM_ClearITPendingBit (TIM16, TIM_IT_Update);  
}
*/

void TIM3_IRQHandler (void) {	                  //таймер на 1 сек
  LED_G_toggle;
	stop_dog++;
	if (stop_dog>120) { 
		RELE1_OFF; RELE2_OFF; DelaymS (200);
		NVIC_SystemReset();	
	}    		                                      // в случае зависания более 3 мин. сброс МК
	IWDG_ReloadCounter(); 			                  // перезагрузка сторожевого таймера, сброс к начальным значениям  
                                                // если работают прерывания, но не заходит в майн, то будет сброс через 2 мин. Если завис так, что и прерывания отключились, то через 26 сек будет сброс		
  if (count_restart>2) NVIC_SystemReset();
	count_restart = 0;
	//Очищаем бит прерывания
	TIM_ClearITPendingBit (TIM3, TIM_IT_Update);
}	



void TIM2_IRQHandler (void) {                   //таймер на 1 сек
	if (!modul_ESP_connect) {						          // при неудачной инициализации или в течении 2 минут неуспешной попытке отправить данные сбрасывается флаг modul_ESP_connect 
    count_modul_disconnect++;
    if (count_modul_disconnect > 120) { 
      count_modul_disconnect = 0; 
      start_initESP = true; 
      led_one_OFF(LED_R);
    }                                           // ждем две минуты и инициализируем модуль
	} else count_modul_disconnect = 0;
	
	if (flag_start_get_dataCOM && !flag_initESP8266 )count_answer_com++;			// было событие приема данных, инкрементируем счетчик паузы между данными
	if (count_answer_com>2) {																								  // конец приема данных
		count_answer_com = 0; 
    flag_start_get_dataCOM = false; 
		end_timeout_getdata = true;
	} 
	count_minute++; seconds_time++;				// count_minute - для отсчета минут(после 60 сбрасывается, seconds_time - для системного времени в секундах)
	seconds_start_server++;
	if (seconds_time >86399) { 
		seconds_time = 0;  											// 86400сек = 24ч, сбрасываем системное время в ноль
		out_for_SMART.count_off_POW = 0;				// сбрасываем время откл. эл. энергии
		out_for_SMART.count_work_HEAT = 0;			// сбрасываем время нагрева
		seconds_start_server = 0;
//		NVIC_SystemReset();		
	}
	if (seconds_time == 85800) flag_send_MAIL = true;  // отправка письма-отчета каждые сутки в 23:50
	/////////////////////////////////////
	if (count_minute == 60) {
		count_minute = 0;
		if (out_for_SMART.status_power220 == status_NO) out_for_SMART.count_off_POW++;
		if (out_for_SMART.status_HEAT == status_OK) out_for_SMART.count_work_HEAT++;
		if (out_for_SMART.count_off_POW > 1440) out_for_SMART.count_off_POW = 0;
		if (out_for_SMART.count_work_HEAT > 1440) out_for_SMART.count_work_HEAT = 0;
	}
// выставляем флаг для заполнения массива rep_day[24][6];	
	if (count_minute == 45 ) {
		if (!start_flag_ever_hour) flag_ever_hour = true;
		start_flag_ever_hour = true;					          // пока время сохраняется  45 мин. флаг start_flag_ever_hour будет true, таким образом flag_ever_hour будет выставлен только 1 раз
	} else start_flag_ever_hour = false;
	if (out_for_SMART.work_TARIF == status_OK) {
		if (seconds_time == time_DAY) {
			out_for_SMART.set_TMP = out_for_SMART.set_TMP - out_for_SMART.gisteresis_TARIF;
			flag_change_TMP = 1;
		}
		if (seconds_time == time_NIGHT) {
		  out_for_SMART.set_TMP = out_for_SMART.set_TMP + out_for_SMART.gisteresis_TARIF;
  		flag_change_TMP = 2;
		}
  }
  if (GPIO_ReadInputDataBit (GPIOA, MODE_BUT)) count_ready_RST_DEF_ESP = 0; 							// возвращает состояние пина 1 или 0
	else { 
    count_ready_RST_DEF_ESP++;  
    // debug for mail //
//    flag_send_MAIL = true;
    //  ////////////  //
  }
  if (count_ready_RST_DEF_ESP > 5) { 
    count_ready_RST_DEF_ESP = 0;
    led_one_ON(LED_R);
    flag_ready_RST_ESP = true;
  }		
	TIM_ClearITPendingBit (TIM2, TIM_IT_Update);
}


uint32_t CalculateCRC (uint8_t dim_crc[], uint32_t size);



void data_transmit_clientWIFI (void) {
	uint16_t i;
  uint32_t crc;
	data_OUT_COM[0] = 'E'; 
  data_OUT_COM[1] = 'Z'; 
  data_OUT_COM[2] = 'A'; 
  data_OUT_COM[3] = 'P';
	for (i = 0; i<6; i++) {
		data_OUT_COM[i+4] = init_param.MAC_adr[i];    // получение мак-адр. не реализовано, он пока произвольно указан в wifi_esp8266.c
	}
  for (i = 0; i<12; i++) {
    data_OUT_COM[i+19] = init_param.IP_adr[i];
  }
	data_OUT_COM[11] = (devID_SERVER+'0') | (devID_TERMO+'0');
	data_OUT_COM[12] = all_number_connect_WIFI;
	data_OUT_COM[31] = init_param.potrserv/1000+'0';
  data_OUT_COM[32] = (init_param.potrserv/100)%10+'0';
	data_OUT_COM[33] = (init_param.potrserv/10)%10+'0';
	data_OUT_COM[34] = init_param.potrserv%10+'0';

  
  for(i = 4; i<64; i++){ if (data_OUT_COM[i]>127)data_OUT_COM[i] = '-';	}
  for(i = 0; i<60; i++){ data_OUT_COM[i+64] = buf_data[i]; }
  crc = CalculateCRC(data_OUT_COM, 123);
  buf_data[60] = (uint8_t)crc;
	buf_data[61] = (uint8_t)((crc & 0xff00)>>8);
  
	for (i = 0; i<64; i++) {
		if(buf_data[i]>127) {								/// ESP не принимает числа больше 127 (Кодировка US ASCII, все что больше 127 кодируется '?'), добавляем еще 64 байта для старшего разряда 1000 0000
			data_OUT_COM[i+128] = 127;
			data_OUT_COM[i+64] = buf_data[i]&0x7F;
		}	else { 
			data_OUT_COM[i+128] = 0; 
			data_OUT_COM[i+64] = buf_data[i];
		}
  }

	len_packet_COM = 192; 			//128;
}


	
void data_answer (void) {
	Send_Buffer[0] = 0x02; 
	Send_Buffer[2] = Report_buf[2];
	Send_Buffer[5] = Report_buf[5];
	if ((PrevXferDone) && (USB_Device_dev.dev.device_status == USB_CONFIGURED)) {
    USBD_HID_SendReport (&USB_Device_dev, Send_Buffer, 64); 
    PrevXferDone = 0;
  }	
	memset(Send_Buffer,0x00,63);
};



extern double alarm_A;
extern double alarm_W;
extern double temp_boiler;
extern double temp_outdoor;

void show_data (uint8_t mode) {
  switch (mode) {
    case 1:
        out_temp_LCDTM1637(alarm_A);
      break;
    case 2:
        if (cnt_for_outDISP == 0) disp_scrsave (1);   // воздух в помещении
        else {
          if (cnt_for_outDISP < 20) out_temp_LCDTM1637(alarm_A);
        }
        if (cnt_for_outDISP == 20) disp_scrsave (2);  // теплоноситель
        else {
          if (cnt_for_outDISP > 20 && cnt_for_outDISP < 40) out_temp_LCDTM1637(alarm_W);
        }
        cnt_for_outDISP++;        
        if (cnt_for_outDISP >= 40) cnt_for_outDISP = 0;        
      break;
    case 3:
        if (cnt_for_outDISP == 0) disp_scrsave (1);   // воздух в помещении
        else {
          if (cnt_for_outDISP < 20) out_temp_LCDTM1637(alarm_A);
        }
        if (cnt_for_outDISP == 20) disp_scrsave (3);  // погода
        else {
          if (cnt_for_outDISP > 20 && cnt_for_outDISP < 40) out_temp_LCDTM1637(temp_outdoor);
        }  
        cnt_for_outDISP++;        
        if (cnt_for_outDISP >= 40) cnt_for_outDISP = 0;        
      break;
    case 4:
        if (cnt_for_outDISP == 0) disp_scrsave (1);   // воздух в помещении
        else {
          if (cnt_for_outDISP < 20) out_temp_LCDTM1637(alarm_A);
        }
        if (cnt_for_outDISP == 20) disp_scrsave (2);  // теплоноситель
        else {
          if (cnt_for_outDISP > 20 && cnt_for_outDISP < 40) out_temp_LCDTM1637(alarm_W);
        }
        if (cnt_for_outDISP == 40) disp_scrsave (3);  // погода
        else {
          if (cnt_for_outDISP > 40 && cnt_for_outDISP < 60) out_temp_LCDTM1637(temp_outdoor);
        }  
        cnt_for_outDISP++;        
        if (cnt_for_outDISP >= 60) cnt_for_outDISP = 0;        
      break;
    case 5:
        if (cnt_for_outDISP == 0) disp_scrsave (1);   // воздух в помещении
        else {
          //if (cnt_for_outDISP < 20) out_temp_LCDTM1637(((double)out_for_SMART.tmp_AIR)/100);
          if (cnt_for_outDISP < 20) out_temp_LCDTM1637(alarm_A);
        }
        if (cnt_for_outDISP == 20) disp_scrsave (2);  // теплоноситель
        else {
          //if (cnt_for_outDISP > 20 && cnt_for_outDISP < 40) out_temp_LCDTM1637(((double)out_for_SMART.tmp_WATER)/100);
          if (cnt_for_outDISP > 20 && cnt_for_outDISP < 40) out_temp_LCDTM1637(alarm_W);
        }
        if (cnt_for_outDISP == 40) disp_scrsave (3);  // погода
        else {
          if (cnt_for_outDISP > 40 && cnt_for_outDISP < 60) out_temp_LCDTM1637(temp_outdoor);
        }  
        if (cnt_for_outDISP == 60) disp_scrsave (4);  // горячая вода
        else {
          if (cnt_for_outDISP > 60 && cnt_for_outDISP < 80) out_temp_LCDTM1637(temp_boiler);
        }  
        cnt_for_outDISP++;        
        if (cnt_for_outDISP >= 80) cnt_for_outDISP = 0;      
      break;
    case 6:
        if (cnt_for_outDISP == 0) disp_scrsave (1);   // воздух в помещении
        else {
          if (cnt_for_outDISP < 20) out_temp_LCDTM1637(alarm_A);
        }
        if (cnt_for_outDISP == 20) disp_scrsave (4);  // горячая вода
        else {
          if (cnt_for_outDISP > 20 && cnt_for_outDISP < 40) out_temp_LCDTM1637(temp_boiler);
        }  
        cnt_for_outDISP++;        
        if (cnt_for_outDISP >= 40) cnt_for_outDISP = 0;          
      break;
    case 7:
        if (cnt_for_outDISP == 0) disp_scrsave (1);   // воздух в помещении
        else {
          if (cnt_for_outDISP < 20) out_temp_LCDTM1637(alarm_A);
        }
        if (cnt_for_outDISP == 20) disp_scrsave (3);  // погода
        else {
          if (cnt_for_outDISP > 20 && cnt_for_outDISP < 40) out_temp_LCDTM1637(temp_outdoor);
        }  
        if (cnt_for_outDISP == 40) disp_scrsave (4);  // горячая вода
        else {
          if (cnt_for_outDISP > 40 && cnt_for_outDISP < 60) out_temp_LCDTM1637(temp_boiler);
        }  
        cnt_for_outDISP++;        
        if (cnt_for_outDISP >= 60) cnt_for_outDISP = 0;        
      break;    
  }

}



extern uint16_t set_tempBoiler;
extern uint16_t set_gisttempBoiler;
extern uint8_t flag_work_boiler;
extern uint16_t change_tmp;
bool save_new_set_flag = false;

uint8_t get_val_for_SET(uint8_t mode) {
  uint8_t val = 0;
  if (status_BUT1 == 0 || status_BUT2 == 0) cnt_set_mode = 0;
  switch (mode){
    case 1:
      val = out_for_SMART.set_TMP;
      if (status_BUT2 == 0 && status_BUT1 == 1) {
        if(val>1) --val;
      }
      if (status_BUT1 == 0 && status_BUT2 == 1) {
        if(val<50) ++val;
      } 
      if (val != out_for_SMART.set_TMP) save_new_set_flag = true;
      out_for_SMART.set_TMP = val;  
//      change_tmp = out_for_SMART.set_TMP;
//      out_for_SMART.COMAND = set_tmp;
      
      break;
    case 2:
      val = (uint8_t)(out_for_SMART.gisteresis_TMP/100);
      if (status_BUT2 == 0 && status_BUT1 == 1) {
        if(val>1) --val;
      }
      if (status_BUT1 == 0 && status_BUT2 == 1) {
        if(val<50) ++val;
      }
      if (val != (uint8_t)(out_for_SMART.gisteresis_TMP/100)) save_new_set_flag = true;
      out_for_SMART.gisteresis_TMP = (uint16_t)val*100;  
//      PARAM.gisteresis_TMP_ = out_for_SMART.gisteresis_TMP;
      
//      out_for_SMART.COMAND = set_GAS;
      
      break;
    case 3:
      val = (uint8_t)(set_tempBoiler/100);
      if (status_BUT2 == 0 && status_BUT1 == 1) {
        if(val>1) --val;
      }
      if (status_BUT1 == 0 && status_BUT2 == 1) {
        if(val<50) ++val;
      }  
      set_tempBoiler = (uint16_t)val*100;    
      break;
    case 4:
      val = (uint8_t)(set_gisttempBoiler/100);
      if (status_BUT2 == 0 && status_BUT1 == 1) {
        if(val>1) --val;
      }
      if (status_BUT1 == 0 && status_BUT2 == 1) {
        if(val<50) ++val;
      } 
      set_gisttempBoiler = (uint16_t)val*100;   
      break;
    case 5:
      if (flag_COOL) val = 1;
      else val = 0;
      if ((status_BUT2 == 0 && status_BUT1 == 1) || (status_BUT2 == 1 && status_BUT1 == 0)) {
        if (val== 1) val = 0;
        else val = 1; 
        save_new_set_flag = true;        
      }
      if (val == 0) {
        flag_COOL = false;
//        out_for_SMART.COMAND = cool_modeOFF;
      } else {
        flag_COOL = true;
//        out_for_SMART.COMAND = cool_modeON;
      }    
      break;
    case 6:
      val = mode_disp;
      if (status_BUT2 == 0 && status_BUT1 == 1) {
        if(val>=2) --val;
        else val = 7;
      }
      if (status_BUT1 == 0 && status_BUT2 == 1) {
        if(val<=6) ++val;
        else val = 1;
      }    
      mode_disp = val;
      break;    
    case 7:
      if (flag_work_boiler == 1) val = 1;
      else val = 0;
      if ((status_BUT2 == 0 && status_BUT1 == 1) || (status_BUT2 == 1 && status_BUT1 == 0)) {
        if (val== 1) val = 0;
        else val = 1;      
      }
      if (val == 0) flag_work_boiler = 0;
      else flag_work_boiler = 1;   
      break;  
    case 8:
      val = out_for_SMART.status_mode_GAS;
      if ((status_BUT2 == 0 && status_BUT1 == 1) || (status_BUT1 == 0 && status_BUT2 == 1)) {
        if (val == status_NO) {
          out_for_SMART.status_mode_GAS = status_OK;
//          PARAM.work_GAS = status_OK;
        }
        else { 
          out_for_SMART.status_mode_GAS = status_NO;
//          PARAM.work_GAS = status_NO;
        }
        save_new_set_flag = true;
//        PARAM.gisteresis_TMP_ = out_for_SMART.gisteresis_TMP;
//        out_for_SMART.COMAND = set_GAS;
      }
   
      break;       
  }
  disp_set_mode(set_mode, val);
  return val;
}



extern bool flag_answer_TCP;

void TIM16_IRQHandler(void)			
{
  
  //    send_str_USB("mail_to_len = ", 5);
  //    out_temp_LCDTM1637(((double)out_for_SMART.tmp_AIR)/100);
  // каждые 200мс заходим и либо выводим в цикле параметры, либо при удержании более3 сек. любой из кнопок платы дисплея переходим к установкам параметров
  // при отсутствии активности этих кнопок более 3 сек. возвращаемся в рабочий режим  
  // для перехода между режимами настройки одновременно нажать обе кнопки
// SET PARAm.
// AIR_ROOM - A-XX              mode 1
// Water Boiler - B-XX          mode 2
// AIR_ROOM_GIST - GaXX         mode 3
// Water Boiler_GIST - GbXX     mode 4
// Heat right - OF 0            mode 5
// Heat invers - OF 1           mode 5
// Loop Display - CL X          mode 6  
// Boiler ON/OFF - B ON         mode 7 
// Boiler ON/OFF - B OF         mode 7  
// Heater1 ON/OFF - H1ON, H2ON  mode 8  /// включение нагревателя 1(эл. котел) или нагревателя 2(газ. котел)
// Mode disp:
//        CL 1 - AIR            
//        CL 2 - loop AIR - Heat
//        CL 3 - loop AIR - Outdoor
//        CL 4 - loop AIR - Outdoor - Heat
//        CL 5 - loop AIR - Heat - Outdoor - Boiler
//        CL 6 - loop AIR - Boiler
//        CL 7 - loop AIR - Outdoor - Boiler  
    LED_G_toggle;
    uint8_t val = 0;
    uint32_t change_tmp = 0;
    
    if (!flag_set_mode) {
      if (status_BUT1 || status_BUT2) {
        TIM_Cmd(TIM16, DISABLE);
        cnt_set_mode = 0;
        show_data(mode_disp);
      } else {
        cnt_set_mode++;
        if (cnt_set_mode > 15) {              // удержание обеих кнопок более 3 сек
          flag_set_mode = true;
          for (uint8_t i=0; i<4; ++i) TM1637_display_segments(i, 0, 0);
          cnt_set_mode = 0;
        }
      }
    } else {
        cnt_set_mode++;
//        if(cnt_set_mode%3 != 0) disp_set_mode(mode_disp, out_for_SMART.set_TMP);
        if(status_BUT1 == 0 && status_BUT2 ==0) {
          set_mode++;
          if (set_mode > 8) set_mode = 1;
          cnt_set_mode = 0;
          val = get_val_for_SET(set_mode);
          DelaymS(1200);
        }
//        else {
          val = get_val_for_SET(set_mode);
//        }
        if (cnt_set_mode%3 != 0) disp_set_mode(set_mode, val);
        else {
          if (status_BUT1 && status_BUT2) {
            for (uint8_t i=0; i<4; ++i) TM1637_display_segments(i, 0, 0);
            DelaymS (300);
          }
        }
        
        if (cnt_set_mode>20) {                // более 4сек. нет активности - выход
          cnt_set_mode = 0;  
          flag_set_mode = false; 
          TM1637_display_segments(0, 0, 0);
          set_mode = 8;
          
/////////////////////////
//          if (out_for_SMART.COMAND == set_GAS || out_for_SMART.COMAND == set_tmp || out_for_SMART.COMAND == cool_modeOFF || out_for_SMART.COMAND == cool_modeON) flag_answer_TCP = true;
          if (save_new_set_flag) {
            save_modework (save_setTMP, out_for_SMART.set_TMP, 0);
            change_tmp = (uint32_t)out_for_SMART.status_mode_GAS | (uint32_t)(out_for_SMART.gisteresis_TMP<<8);
            save_modework (save_work_modeGAS, change_tmp, 0);
            if (flag_COOL) save_modework (mode_cool_heat, cool_modeON, 0);
            else save_modework (mode_cool_heat, cool_modeOFF, 0);
            save_new_set_flag = false;
          }
/////////////////////////          
        }    
    }
////////////

//////////    
    TIM_ClearITPendingBit(TIM16, TIM_IT_Update);
    TIM_Cmd(TIM16, ENABLE);
}
