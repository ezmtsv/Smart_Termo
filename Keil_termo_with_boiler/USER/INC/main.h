/**
  ******************************************************************************
  * @file    USB_Example/main.h 
  * @author  MCD Application Team
  * @version V1.0.0
  * @date    17-January-2014
  * @brief   Header for main.c module
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
  
/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __MAIN_H
#define __MAIN_H

/* Includes ------------------------------------------------------------------*/
#include "stm32f0xx.h"
//#include "stm32f072b_discovery.h"
//#include "tsl_types.h"
//#include "tsl_user.h"
#include "usbd_hid_core.h"
#include "usbd_usr.h"
#include "usbd_desc.h"
#include "usbd_custom_hid_core.h"
#include "user_init.h"
#include "test.h"
#include "func.h"
#include "usart_stm.h"
#include "wifi_esp8266.h"
//#include "LCD_1602.h"
#include "TM1637.h"

#define NVIC_VectTab_FLASH           					 ((uint32_t)0x08004000)
#define BOOTLOADER_KEY_START_ADDRESS            (uint32_t)0x08003800
#define MAIN_PROGRAM_START_ADDRESS              (uint32_t)0x08004000
#define START_ADDRESS              							(uint32_t)0x08000000
#define MAIN_PROGRAM_PAGE_NUMBER                9
#define NUM_OF_PAGES                            64
#define FLASH_PAGE_SIZE                         2048
#define mode_programm				                    0x77700777
#define mode_wait				                    		0x11221122
#define erase 																	':'
#define save_setTMP															8
#define save_work_modeGAS												9
#define save_work_modeTARIF											10
#define save_time_modeTARIF											11								// 
#define time_for_save														13
#define mode_cool_heat													14




#define ADDRESS_KORR_COEFF						          (uint32_t)0x08003000
//#define NVIC_VectTab_RAM	           					 ((uint32_t)0x08005000)
/* Exported types ------------------------------------------------------------*/
/* Exported constants --------------------------------------------------------*/
/* Exported macro ------------------------------------------------------------*/
/* Exported functions ------------------------------------------------------- */
void TSL_USB_Test(void);
void save_net(uint8_t data[], uint8_t flag, uint16_t len);
void read_FLASH_CORR(void);
void save_coeff_cor(uint8_t dim[]);
void out_echo_from_ESP(void);
void read_modework(void);
void save_modework(int shift, uint32_t data, uint32_t data1);

//void NVIC_SetVectorTable(uint32_t NVIC_VectTab, uint32_t Offset);
#endif /* __MAIN_H */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
