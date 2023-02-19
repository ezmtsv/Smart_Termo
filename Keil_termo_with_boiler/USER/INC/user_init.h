#ifndef __USER_INIT_H
#define __USER_INIT_H

#include "stm32f0xx.h"

//GPIOA/
#define PORT_ADC                        GPIOA
#define ADC_GPIO_CLK                    RCC_AHBPeriph_GPIOA
#define CONTR_CHARGE                    GPIO_Pin_0
#define pinCHARGE_ON                    (GPIOA->BSRR |= CONTR_CHARGE)
#define pinCHARGE_OFF                   (GPIOA->BRR |= CONTR_CHARGE)
#define ESP_PROG                        GPIO_Pin_1
#define ESP_PROG_ON                    (GPIOA->BSRR |= ESP_PROG)
#define ESP_PROG_OFF                   (GPIOA->BRR |= ESP_PROG)
#define RELE1                           GPIO_Pin_4
#define RELE1_ON                        (GPIOA->BSRR|=RELE1)
#define RELE1_OFF                       (GPIOA->BRR|=RELE1)
#define ADC_IN5                         GPIO_Pin_5  
#define ADC_IN6                         GPIO_Pin_6
#define ADC_CHARGE                      GPIO_Pin_7
//#define ADC_IN8                         GPIO_Pin_8
//#define ADC_IN9                         GPIO_Pin_9
#define MODE_BUT                        GPIO_Pin_8
#define STATUS_220                      GPIO_Pin_9
#define status_STATUS_220               ((GPIOA->IDR & STATUS_220)>>9)

#define LED_G                           GPIO_Pin_10
#define LED_G_toggle                    (GPIOA->ODR ^= LED_G)
#define LED_R                           GPIO_Pin_13
#define LED_R_toggle                    (GPIOA->ODR ^= LED_R)
#define RST_ESP8266                     GPIO_Pin_15
#define RST_ESP8266_ON                  (GPIOA->BRR|=RST_ESP8266)
#define RST_ESP8266_OFF                 (GPIOA->BSRR|=RST_ESP8266)

//GPIOB
//#define MODE_BUT                        GPIO_Pin_0
//#define STATUS_220                      GPIO_Pin_1
#define ADC_IN8                         GPIO_Pin_0
#define ADC_IN9                         GPIO_Pin_1

#define RELE2                           GPIO_Pin_2
#define RELE2_ON                        (GPIOB->BSRR|=RELE2)
#define RELE2_OFF                       (GPIOB->BRR|=RELE2)
#define RELE3                           GPIO_Pin_5
#define RELE3_ON                        (GPIOB->BSRR|=RELE3)
#define RELE3_OFF                       (GPIOB->BRR|=RELE3)
#define OV_CUR                          GPIO_Pin_6
#define BUT1                            GPIO_Pin_7
#define BUT2                            GPIO_Pin_8
#define status_BUT1						          ((GPIOB->IDR & BUT1)>>7)
#define status_BUT2						          ((GPIOB->IDR & BUT2)>>8)
#define RED_DISP                        GPIO_Pin_9
#define GREEN_DISP                      GPIO_Pin_10
#define RED_DISP_OFF                    (GPIOB->BRR |= RED_DISP)
#define RED_DISP_ON                     (GPIOB->BSRR |= RED_DISP)
#define GREEN_DISP_OFF                  (GPIOB->BRR |= GREEN_DISP)
#define GREEN_DISP_ON                   (GPIOB->BSRR |= GREEN_DISP)


#define TIMER_PRESCALER                 48000
#define TIMER_PRESCALER_tim2            48000

#define  cmd_noreq_1s                   0x83
#define  cmd_noreq_15s                  0x84
#define  cmd_req                        0x34
#define  cmd_save_kf                    0x24
#define  status_OK                      0x55
#define  status_NO                      0x33
#define  set_tmp                        0x44
//#define  dec_tmp                        0x54
#define  synchro 						            0x64  // синхронизировать время
#define  set_GAS						            0x75
#define  set_work_TARIF                 0x78
#define  synchro_timeTARIF              0x67
#define  cool_modeON                    0x39  // вкл. режим охлажд.(инверсный выход)
#define  cool_modeOFF                   0x40  // выкл. режим охлажд.(инверсный выход)
#define  set_link                       0x11
#define  config_mail                    0x12  // настройки эл. почты
#define  load_def						            0x19  //  сброс всех настроек  к настройкам по умолчанию
#define  set_tmpBR						          0x20  //  установить температуру воды в бойлере
#define  set_gisttmpBR						      0x21  //  установить гистерезис темп. воды в бойлере
#define  set_ONOFFboiler                0x22  // вкл./выкл. бойлер (0 - выключен, любое значение включен) 
#define  setFlagAlarmONOF               0x23  // аварийное включение/выключение котла

#define devID_SERVER                 	((uint8_t)0x01)
#define devID_CLIENT                 	((uint8_t)0x00)
#define devID_TERMO                 	((uint8_t)0x02)
#define devID_POW                 		((uint8_t)0x04)
#define devID_LAMP                 		((uint8_t)0x06)

void led_one_ON (uint16_t led_);
void led_one_OFF (uint16_t led_);
void init_tmr (void);
void init_tmr2 (void);
void init_tmr16 (void);
void init_inter_ref(void);
void init_gpio_(void);
void init_ADC_STM(void);
void iwdg_init(void);

#endif
