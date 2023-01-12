#ifndef __FUNC_H
#define __FUNC_H

#include "stm32f0xx.h"
#include "user_init.h"

#define alarm_tempAIR 		3.00
#define alarm_tempWATER 	95.00

///////////////////// параметры для передачи смартфону
typedef struct {
	uint16_t count_off_POW;
	uint16_t count_work_HEAT;
	uint8_t	status_BUSY;
	uint8_t	status_HEAT;
	uint8_t status_power220;
	uint16_t tmp_AIR;
	uint16_t tmp_WATER;
	uint8_t set_TMP;
	uint8_t status_mode_GAS;
	uint8_t work_TARIF;
	uint16_t gisteresis_TMP;
	uint8_t gisteresis_TARIF;	
	uint8_t znaki_TMP;								//1-й бит = "0" - темп. возд. со знаком "+", если "1" - значит "-", для 2-го бита тоже, только для воды
	uint8_t count_COMAND;
	uint8_t COMAND;
	int SECOND;
	uint16_t tmp_W;
	uint16_t tmp_A;
	uint16_t tmp_B;
	uint16_t tmp_O; 
	uint16_t tmp_Boiler;
	uint16_t tmp_Outdoor;  
} buffer_OUT_SMART;

///////////////////// параметры принятые от смартфона
typedef struct {
	int time_NIGHT_;
	int time_DAY_;
	uint8_t work_GAS;
	uint8_t work_TARIF_;
	uint8_t set_TMP_;
	int SECOND_;
	uint16_t gisteresis_TMP_;
	uint8_t gisteresis_TARIF_;
	uint8_t delta_tmp_;
	uint8_t sys_DATA[4];
	uint8_t count_COMAND_;
	uint8_t COMAND_;
  uint16_t TMPBR_;
  uint8_t gistTMPBR_;
  uint8_t flagBR_;
  uint8_t flag_AlarmOFF;
} work_param;

//////////////////////////////////////////////////
void work_ADC_STM (void);
uint16_t run_ADCSTM (void);
void init_out_for_SMART (buffer_OUT_SMART* out);
void real_temp (void);

#endif
