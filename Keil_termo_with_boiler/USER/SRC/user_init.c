#include "user_init.h"
#include "func.h"



void init_PORT_ADC (void) {
  GPIO_InitTypeDef  GPIO_InitStructure;
    
  // init output	
  RCC -> AHBENR |= RCC_AHBPeriph_GPIOA;	
	
  GPIO_InitStructure.GPIO_Pin = RST_ESP8266 | RELE1 | LED_G | LED_R | CONTR_CHARGE | ESP_PROG;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_Init (PORT_ADC, &GPIO_InitStructure);	
	
  // init analog. func	
//  GPIO_InitStructure.GPIO_Pin = ADC_IN5 | ADC_IN6 | ADC_CHARGE | ADC_IN8 | ADC_IN9; 
  GPIO_InitStructure.GPIO_Pin = ADC_IN5 | ADC_IN6 | ADC_CHARGE;  
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AN;
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;                  // работает только в случае настройки пина как GPIO_Mode_IN и GPIO_Mode_AN
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init (PORT_ADC, &GPIO_InitStructure);
  
  // init input		
	GPIO_InitStructure.GPIO_Pin = STATUS_220 | MODE_BUT;  
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_Init (GPIOA, &GPIO_InitStructure);  
  
  ESP_PROG_ON;
}



void init_PORT_B (void) {
	GPIO_InitTypeDef  GPIO_InitStructure;
  
// init output	
	RCC -> AHBENR |= RCC_AHBPeriph_GPIOB;
  
	GPIO_InitStructure.GPIO_Pin = RELE2 | RELE3 | GREEN_DISP | RED_DISP;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_Init (GPIOB, &GPIO_InitStructure);
  
  // init input		
//	GPIO_InitStructure.GPIO_Pin = STATUS_220 | MODE_BUT | OV_CUR | BUT1 | BUT2;
	GPIO_InitStructure.GPIO_Pin = OV_CUR | BUT1 | BUT2;  
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_Init (GPIOB, &GPIO_InitStructure);
  
  // init analog. func	
  GPIO_InitStructure.GPIO_Pin = ADC_IN8 | ADC_IN9;  
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AN;
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;                  // работает только в случае настройки пина как GPIO_Mode_IN и GPIO_Mode_AN
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init (GPIOB, &GPIO_InitStructure);  
  
  GREEN_DISP_OFF;
  RED_DISP_OFF;
  RELE3_OFF;
}



void led_one_ON (uint16_t led_) {
	GPIOA-> BSRR = led_;
}



void led_one_OFF (uint16_t led_) {
	GPIOA-> BRR = led_;
}



void init_tmr (void) {
  TIM_TimeBaseInitTypeDef timer;
  RCC_APB1PeriphClockCmd (RCC_APB1Periph_TIM3, ENABLE);
  TIM_TimeBaseStructInit (&timer);
  
  timer.TIM_Prescaler = TIMER_PRESCALER;          // Выставляем предделитель
  // Тут значение, досчитав до которого таймер сгенерирует прерывание
  timer.TIM_Period = 1000-1;						          // настройка на 1000мс
  TIM_TimeBaseInit(TIM3, &timer);                 // Инициализируем TIM3 нашими значениями	
	// Настраиваем таймер для генерации прерывания по обновлению (переполнению)
  TIM_ITConfig(TIM3, TIM_IT_Update, ENABLE);
  // Запускаем таймер
  // TIM_Cmd(TIM3, ENABLE);
  // Разрешаем соответствующее прерывание
  NVIC_EnableIRQ(TIM3_IRQn);
}



void init_tmr2 (void) {
  TIM_TimeBaseInitTypeDef timer;
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2, ENABLE);
	TIM_TimeBaseStructInit(&timer);
  // Выставляем предделитель
  timer.TIM_Prescaler = TIMER_PRESCALER_tim2;
  // Тут значение, досчитав до которого таймер сгенерирует прерывание
  // timer.TIM_Period = 1000-1;						//////////////// настройка на 1000мс
	timer.TIM_Period = 999;
  // Инициализируем TIM3 нашими значениями
  TIM_TimeBaseInit(TIM2, &timer);	
	// Настраиваем таймер для генерации прерывания по обновлению (переполнению)
	TIM_ITConfig(TIM2, TIM_IT_Update, ENABLE);
	// Запускаем таймер
  // TIM_Cmd(TIM3, ENABLE);
	// Разрешаем соответствующее прерывание
	TIM_ARRPreloadConfig(TIM2, ENABLE);	// конфигурируем запись в рабочем режиме нового значения периода таймера
	NVIC_EnableIRQ(TIM2_IRQn);
}


void init_tmr16 (void) {
	TIM_TimeBaseInitTypeDef timer;
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_TIM16, ENABLE);
	TIM_TimeBaseStructInit(&timer);
  timer.TIM_Prescaler = 48000-1;
	timer.TIM_Period = 200;             // 200ms 
  TIM_TimeBaseInit(TIM16, &timer);	
	TIM_ITConfig(TIM16, TIM_IT_Update, ENABLE);
  NVIC_SetPriority (TIM16_IRQn, 1);
  NVIC_EnableIRQ(TIM16_IRQn);
  TIM_Cmd(TIM16, ENABLE);
}



static void init_GPIO_ineterrupt (void) {
  //////////////////////PORT GPIOA GPIOC//////////////////////////////////	
	////////////////подключение прерываний от выбраных пинов и портов////////////
	RCC->APB2RSTR = 0; // включение SYSCFG контроллера
	RCC->APB2ENR |= RCC_APB2Periph_SYSCFG; // подключение тактирования SYSCFG контроллера
	
//	SYSCFG->EXTICR[0] = SYSCFG_EXTICR1_EXTI0_PB | SYSCFG_EXTICR1_EXTI1_PB;
//	SYSCFG->EXTICR[1] = SYSCFG_EXTICR2_EXTI5_PA | SYSCFG_EXTICR2_EXTI6_PA;
//	SYSCFG->EXTICR[3] = SYSCFG_EXTICR4_EXTI15_PA;
	SYSCFG->EXTICR[2] = SYSCFG_EXTICR3_EXTI8_PA | SYSCFG_EXTICR3_EXTI9_PA; 
	
//	EXTI->IMR = EXTI_IMR_MR0 | EXTI_IMR_MR1 | EXTI_IMR_MR2 | EXTI_IMR_MR3 | EXTI_IMR_MR4 | EXTI_IMR_MR5 | EXTI_IMR_MR6 | EXTI_IMR_MR7; 
//	EXTI ->RTSR = EXTI_RTSR_TR0 | EXTI_RTSR_TR1 | EXTI_RTSR_TR2 | EXTI_RTSR_TR3 | EXTI_RTSR_TR4 | EXTI_RTSR_TR5 | EXTI_RTSR_TR6 | EXTI_RTSR_TR7;// прерывание по фронту разрешено
//	EXTI ->FTSR = EXTI_FTSR_TR4;	// | EXTI_RTSR_TR7;
	EXTI->IMR = EXTI_IMR_MR8 | EXTI_IMR_MR9;
	EXTI ->RTSR = EXTI_RTSR_TR8 | EXTI_RTSR_TR9; //прерывания по спаду разрешены
	EXTI ->FTSR = EXTI_FTSR_TR9;			//прерывания по фронту разрешены
	
//	NVIC_SetPriority (EXTI0_1_IRQn, 1);
//	NVIC_SetPriority (EXTI2_3_IRQn, 1); 		// установка приоритета
	NVIC_SetPriority (EXTI4_15_IRQn, 0);
	NVIC_EnableIRQ (EXTI4_15_IRQn);
//	NVIC_EnableIRQ (EXTI0_1_IRQn);
}



void init_gpio_ (void) {
	init_PORT_ADC ();									// инициализация GPIOA микроконтроллера
	init_PORT_B ();										// инициализация GPIOB микроконтроллера
	init_GPIO_ineterrupt ();
}



void init_ADC_STM (void) {
	ADC_InitTypeDef stuct_ADC_STM;
	RCC_APB2PeriphClockCmd (RCC_APB2Periph_ADC1, ENABLE);

	stuct_ADC_STM.ADC_Resolution = ADC_Resolution_12b;
	stuct_ADC_STM.ADC_ContinuousConvMode = DISABLE;   										// не сканировать каналы, просто измерить один канал
	stuct_ADC_STM.ADC_DataAlign = ADC_DataAlign_Right;
	stuct_ADC_STM.ADC_ExternalTrigConvEdge = ADC_ExternalTrigConvEdge_None;
	
	ADC_StructInit (&stuct_ADC_STM);

// настройка канала Vref  
//	ADC_ChannelConfig (ADC1, ADC_CHSELR_CHSEL17, ADC_SampleTime_1_5Cycles);
//	VREFINT_DATA  = ADC_GetCalibrationFactor (ADC1);
// настройка канала
//	ADC_ChannelConfig (ADC1, ADC_CHSELR_CHSEL3, ADC_SampleTime_1_5Cycles);
	ADC_ChannelConfig (ADC1, ADC_CHSELR_CHSEL6, ADC_SampleTime_239_5Cycles);
	ADC_GetCalibrationFactor (ADC1);

	ADC_Cmd (ADC1, ENABLE);
}



void iwdg_init (void) {
	// включаем LSI
	RCC_LSICmd (ENABLE);
	while (RCC_GetFlagStatus(RCC_FLAG_LSIRDY) == RESET);
	// разрешается доступ к регистрам IWDG
	IWDG_WriteAccessCmd (IWDG_WriteAccess_Enable);
	// устанавливаем предделитель
	IWDG_SetPrescaler (IWDG_Prescaler_256);		// 40kHz , предделитель от 4 до 256, берем 256
	// значение для перезагрузки
	IWDG_SetReload (0xFFF);										// максимальное значение, так как 12 разрядов 4095
	// перезагрузим значение
	IWDG_ReloadCounter();
	// LSI должен быть включен
	IWDG_Enable();
}



/*
Для работы USB с МК STM32F070 используем внешний кварц, в файле system_stm32f0xx.c нужно закоментить строчку #define PLL_SOURCE_HSI48
и раскоментить строку #define PLL_SOURCE_HSE
Далее в файле usb_conf.h надо закоментить строку #define USB_CLOCK_SOURCE_CRS
В настройках проекта на вкладке Linker обязательно указать файл *.sct, он отвечает за настройку регионов памяти МК 
*/
