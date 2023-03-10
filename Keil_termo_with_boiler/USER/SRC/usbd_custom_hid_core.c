/**
  ******************************************************************************
  * @file    usbd_hid_core.c
  * @author  MCD Application Team
  * @version V1.0.0
  * @date    17-January-2014
  * @brief   This file provides the HID core functions.
  *
  * @verbatim
  *      
  *          ===================================================================      
  *                                HID Class  Description
  *          =================================================================== 
  *           This module manages the HID class V1.11 following the "Device Class Definition
  *           for Human Interface Devices (HID) Version 1.11 Jun 27, 2001".
  *           This driver implements the following aspects of the specification:
  *             - The Boot Interface Subclass
  *             - The Mouse protocol
  *             - Usage Page : Generic Desktop
  *             - Usage : Custom
  *             - Collection : Application 
  *           
  *      
  *  @endverbatim
  *
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
#include "usbd_custom_hid_core.h"
#include "usbd_desc.h"
#include "usbd_req.h"
#include "main.h"
#include <string.h>


void read_FLASH_CORR(void);
void save_coeff_cor(uint8_t dim[]);
bool status_LED = false;
uint32_t buf_COEFF[256];
/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
/* Private function prototypes -----------------------------------------------*/ 
uint8_t  USBD_HID_Init (void  *pdev, 
                               uint8_t cfgidx);

uint8_t  USBD_HID_DeInit (void  *pdev, 
                                 uint8_t cfgidx);

uint8_t  USBD_HID_Setup (void  *pdev, 
                                USB_SETUP_REQ *req);

uint8_t  *USBD_HID_GetCfgDesc (uint8_t speed, uint16_t *length);


uint8_t  USBD_HID_DataIn (void  *pdev, uint8_t epnum);


uint8_t  USBD_HID_DataOut (void  *pdev, uint8_t epnum);


uint8_t  USBD_HID_EP0_RxReady (void  *pdev);

USBD_Class_cb_TypeDef  USBD_HID_cb = 
{
  USBD_HID_Init,
  USBD_HID_DeInit,
  USBD_HID_Setup,
  NULL, /*EP0_TxSent*/  
  USBD_HID_EP0_RxReady, /*EP0_RxReady*/ /* STATUS STAGE IN */
  USBD_HID_DataIn, /*DataIn*/
  USBD_HID_DataOut, /*DataOut*/
  NULL, /*SOF */    
  USBD_HID_GetCfgDesc, 
};
/////////////////////////////////////////////////////////////////////
/*
uint32_t CRC32_out;
																 uint32_t CRCTable[256] = {
  0x00000000, 0x77073096, 0xEE0E612C, 0x990951BA, 0x076DC419, 0x706AF48F, 0xE963A535, 0x9E6495A3,
  0x0EDB8832, 0x79DCB8A4, 0xE0D5E91E, 0x97D2D988, 0x09B64C2B, 0x7EB17CBD, 0xE7B82D07, 0x90BF1D91,
  0x1DB71064, 0x6AB020F2, 0xF3B97148, 0x84BE41DE, 0x1ADAD47D, 0x6DDDE4EB, 0xF4D4B551, 0x83D385C7,
  0x136C9856, 0x646BA8C0, 0xFD62F97A, 0x8A65C9EC, 0x14015C4F, 0x63066CD9, 0xFA0F3D63, 0x8D080DF5,
  0x3B6E20C8, 0x4C69105E, 0xD56041E4, 0xA2677172, 0x3C03E4D1, 0x4B04D447, 0xD20D85FD, 0xA50AB56B,
  0x35B5A8FA, 0x42B2986C, 0xDBBBC9D6, 0xACBCF940, 0x32D86CE3, 0x45DF5C75, 0xDCD60DCF, 0xABD13D59,
  0x26D930AC, 0x51DE003A, 0xC8D75180, 0xBFD06116, 0x21B4F4B5, 0x56B3C423, 0xCFBA9599, 0xB8BDA50F,
  0x2802B89E, 0x5F058808, 0xC60CD9B2, 0xB10BE924, 0x2F6F7C87, 0x58684C11, 0xC1611DAB, 0xB6662D3D,
  0x76DC4190, 0x01DB7106, 0x98D220BC, 0xEFD5102A, 0x71B18589, 0x06B6B51F, 0x9FBFE4A5, 0xE8B8D433,
  0x7807C9A2, 0x0F00F934, 0x9609A88E, 0xE10E9818, 0x7F6A0DBB, 0x086D3D2D, 0x91646C97, 0xE6635C01,
  0x6B6B51F4, 0x1C6C6162, 0x856530D8, 0xF262004E, 0x6C0695ED, 0x1B01A57B, 0x8208F4C1, 0xF50FC457,
  0x65B0D9C6, 0x12B7E950, 0x8BBEB8EA, 0xFCB9887C, 0x62DD1DDF, 0x15DA2D49, 0x8CD37CF3, 0xFBD44C65,
  0x4DB26158, 0x3AB551CE, 0xA3BC0074, 0xD4BB30E2, 0x4ADFA541, 0x3DD895D7, 0xA4D1C46D, 0xD3D6F4FB,
  0x4369E96A, 0x346ED9FC, 0xAD678846, 0xDA60B8D0, 0x44042D73, 0x33031DE5, 0xAA0A4C5F, 0xDD0D7CC9,
  0x5005713C, 0x270241AA, 0xBE0B1010, 0xC90C2086, 0x5768B525, 0x206F85B3, 0xB966D409, 0xCE61E49F,
  0x5EDEF90E, 0x29D9C998, 0xB0D09822, 0xC7D7A8B4, 0x59B33D17, 0x2EB40D81, 0xB7BD5C3B, 0xC0BA6CAD,
  0xEDB88320, 0x9ABFB3B6, 0x03B6E20C, 0x74B1D29A, 0xEAD54739, 0x9DD277AF, 0x04DB2615, 0x73DC1683,
  0xE3630B12, 0x94643B84, 0x0D6D6A3E, 0x7A6A5AA8, 0xE40ECF0B, 0x9309FF9D, 0x0A00AE27, 0x7D079EB1,
  0xF00F9344, 0x8708A3D2, 0x1E01F268, 0x6906C2FE, 0xF762575D, 0x806567CB, 0x196C3671, 0x6E6B06E7,
  0xFED41B76, 0x89D32BE0, 0x10DA7A5A, 0x67DD4ACC, 0xF9B9DF6F, 0x8EBEEFF9, 0x17B7BE43, 0x60B08ED5,
  0xD6D6A3E8, 0xA1D1937E, 0x38D8C2C4, 0x4FDFF252, 0xD1BB67F1, 0xA6BC5767, 0x3FB506DD, 0x48B2364B,
  0xD80D2BDA, 0xAF0A1B4C, 0x36034AF6, 0x41047A60, 0xDF60EFC3, 0xA867DF55, 0x316E8EEF, 0x4669BE79,
  0xCB61B38C, 0xBC66831A, 0x256FD2A0, 0x5268E236, 0xCC0C7795, 0xBB0B4703, 0x220216B9, 0x5505262F,
  0xC5BA3BBE, 0xB2BD0B28, 0x2BB45A92, 0x5CB36A04, 0xC2D7FFA7, 0xB5D0CF31, 0x2CD99E8B, 0x5BDEAE1D,
  0x9B64C2B0, 0xEC63F226, 0x756AA39C, 0x026D930A, 0x9C0906A9, 0xEB0E363F, 0x72076785, 0x05005713,
  0x95BF4A82, 0xE2B87A14, 0x7BB12BAE, 0x0CB61B38, 0x92D28E9B, 0xE5D5BE0D, 0x7CDCEFB7, 0x0BDBDF21,
  0x86D3D2D4, 0xF1D4E242, 0x68DDB3F8, 0x1FDA836E, 0x81BE16CD, 0xF6B9265B, 0x6FB077E1, 0x18B74777,
  0x88085AE6, 0xFF0F6A70, 0x66063BCA, 0x11010B5C, 0x8F659EFF, 0xF862AE69, 0x616BFFD3, 0x166CCF45,
  0xA00AE278, 0xD70DD2EE, 0x4E048354, 0x3903B3C2, 0xA7672661, 0xD06016F7, 0x4969474D, 0x3E6E77DB,
  0xAED16A4A, 0xD9D65ADC, 0x40DF0B66, 0x37D83BF0, 0xA9BCAE53, 0xDEBB9EC5, 0x47B2CF7F, 0x30B5FFE9,
  0xBDBDF21C, 0xCABAC28A, 0x53B39330, 0x24B4A3A6, 0xBAD03605, 0xCDD70693, 0x54DE5729, 0x23D967BF,
  0xB3667A2E, 0xC4614AB8, 0x5D681B02, 0x2A6F2B94, 0xB40BBE37, 0xC30C8EA1, 0x5A05DF1B, 0x2D02EF8D
};
																 
//???????????? ???? ??????????
uint32_t flash_read(uint32_t address);

uint32_t CalculateCRC (uint32_t addr_, size_t size) {
  uint32_t  CRC32 = 0xffffffff, fl_data;
	int jj;
	unsigned char buf[4];
	
  while ( size ) {
			fl_data = flash_read(addr_);
			buf[0] = (uint8_t)fl_data;
			buf[1] = (uint8_t)(fl_data >> 8);
			buf[2] = (uint8_t)(fl_data >> 16);
			buf[3] = (uint8_t)(fl_data >> 24);
		for(jj = 0; jj<4; jj++){
			CRC32 = (CRC32>>8) ^ CRCTable[(unsigned char)CRC32 ^ buf[jj]];
		}
		addr_+=4;
		size-=4;
  }
  return CRC32^0xFFFFFFFF;
}
*/
/////////////////////////////////////////////////////////////////////


//??uint8_t Report_buf[135];//65
//??extern uint8_t Send_Buffer[135];
uint8_t Report_buf[64];//65
extern uint8_t Send_Buffer[64];
extern buffer_OUT_SMART out_for_SMART;
extern work_param PARAM;
extern int dataINint[34];

uint8_t USBD_HID_Report_ID=0;
uint8_t flag = 0;
extern uint8_t PrevXferDone;

static uint32_t  USBD_HID_AltSet = 0;
    
static uint32_t  USBD_HID_Protocol = 0;
 
static uint32_t  USBD_HID_IdleState = 0;

/* USB HID device Configuration Descriptor */
const uint8_t USBD_HID_CfgDesc[CUSTOMHID_SIZ_CONFIG_DESC] =
{
  0x09, /* bLength: Configuration Descriptor size */
  USB_CONFIGURATION_DESCRIPTOR_TYPE, /* bDescriptorType: Configuration */
  CUSTOMHID_SIZ_CONFIG_DESC,
  /* wTotalLength: Bytes returned */
  0x00,
  0x01,         /*bNumInterfaces: 1 interface*/
  0x01,         /*bConfigurationValue: Configuration value*/
  0x00,         /*iConfiguration: Index of string descriptor describing
  the configuration*/
  0xC0,         /*bmAttributes: bus powered and Support Remote Wake-up */
  0x32,         /*MaxPower 100 mA: this current is used for detecting Vbus*/
  
  /************** Descriptor of Custom HID interface ****************/
  /* 09 */
  0x09,         /*bLength: Interface Descriptor size*/
  USB_INTERFACE_DESCRIPTOR_TYPE,/*bDescriptorType: Interface descriptor type*/
  0x00,         /*bInterfaceNumber: Number of Interface*/
  0x00,         /*bAlternateSetting: Alternate setting*/
  0x02,         /*bNumEndpoints*/
  0x03,         /*bInterfaceClass: HID*/
  0x00,         /*bInterfaceSubClass : 1=BOOT, 0=no boot*/
  0x00,         /*nInterfaceProtocol : 0=none, 1=keyboard, 2=mouse*/
  0,            /*iInterface: Index of string descriptor*/
  /******************** Descriptor of Custom HID ********************/
  /* 18 */
  0x09,         /*bLength: HID Descriptor size*/
  HID_DESCRIPTOR_TYPE, /*bDescriptorType: HID*/
  0x11,         /*bcdHID: HID Class Spec release number*/
  0x01,
  0x00,         /*bCountryCode: Hardware target country*/
  0x01,         /*bNumDescriptors: Number of HID class descriptors to follow*/
  0x22,         /*bDescriptorType*/
  CUSTOMHID_SIZ_REPORT_DESC,/*wItemLength: Total length of Report descriptor*/
  0x00,
  /******************** Descriptor of Custom HID endpoints ***********/
  /* 27 */
  0x07,          /* bLength: Endpoint Descriptor size */
  USB_ENDPOINT_DESCRIPTOR_TYPE, /* bDescriptorType: */
  
  HID_IN_EP,     /* bEndpointAddress: Endpoint Address (IN) */
  0x03,          /* bmAttributes: Interrupt endpoint */
  HID_IN_PACKET, /* wMaxPacketSize: 2 Bytes max */
  0x00,
  0x20,          /* bInterval: Polling Interval (32 ms) */
  /* 34 */
  
  0x07,	         /* bLength: Endpoint Descriptor size */
  USB_ENDPOINT_DESCRIPTOR_TYPE,	/* bDescriptorType: */
  /*	Endpoint descriptor type */
  HID_OUT_EP,	/* bEndpointAddress: */
  /*	Endpoint Address (OUT) */
  0x03,	/* bmAttributes: Interrupt endpoint */
  HID_OUT_PACKET,	/* wMaxPacketSize: 2 Bytes max  */
  0x00,
  0x20,	/* bInterval: Polling Interval (20 ms) */
  /* 41 */
} ;
const uint8_t CustomHID_ReportDescriptor[CUSTOMHID_SIZ_REPORT_DESC] =
{
  0x06, 0xFF, 0x00,      // USAGE_PAGE (Vendor Page: 0xFF00) //                       
  0x09, 0x01,            // USAGE (Demo Kit)               //    
  0xa1, 0x01,            // COLLECTION (Application)       //            
  // 6 //
  
  // OUT//        
  0x85, 0x01,            //     REPORT_ID (1)		     //
  0x09, 0x01,            //     USAGE (LED 1)	             //
  0x15, 0x00,                    //   LOGICAL_MINIMUM (0)
  0x26, 0xff, 0x00,              //   LOGICAL_MAXIMUM (255)           
  0x75, 0x08,            //     REPORT_SIZE (8)            //        
  0x95, 63, //??0x95, 134,            //     REPORT_COUNT (64)           //       
  0xB1, 0x82,             //    FEATURE (Data,Var,Abs,Vol) //     
  
  0x85, 0x01,            //     REPORT_ID (1)              //
  0x09, 0x01,            //     USAGE (LED 1)              //
  0x91, 0x82,            //     OUTPUT (Data,Var,Abs,Vol)  //

	  // IN //
  0x85, 0x02,            //     REPORT_ID (2)              //         
  0x09, 0x02,            //     USAGE (COMP IN)             //          
  0x15, 0x00,            //     LOGICAL_MINIMUM (0)        //               
  0x26, 0xff, 0x00,      //     LOGICAL_MAXIMUM (255)      //                 
  0x75, 0x08,            //     REPORT_SIZE (8)            //
  0x95, 63, //??0x95, 134,            //     REPORT_COUNT (64)           //	
  0x81, 0x82,            //     INPUT (Data,Var,Abs,Vol)   //                    
  0x85, 0x02,            //     REPORT_ID (2)              //                 
  0x09, 0x02,            //     USAGE (COMP in)             //                     
  0xb1, 0x82,            //     FEATURE (Data,Var,Abs,Vol) //                                 
  // 161 //
  
  0xc0 	          //     END_COLLECTION	             //
	}; // CustomHID_ReportDescriptor //

/* Private function ----------------------------------------------------------*/ 
/**
  * @brief  USBD_HID_Init
  *         Initialize the HID interface
  * @param  pdev: device instance
  * @param  cfgidx: Configuration index
  * @retval status
  */
uint8_t  USBD_HID_Init (void  *pdev, uint8_t cfgidx)
{
  DCD_PMA_Config(pdev , HID_IN_EP,USB_SNG_BUF,HID_IN_TX_ADDRESS);
  DCD_PMA_Config(pdev , HID_OUT_EP,USB_SNG_BUF,HID_OUT_RX_ADDRESS);

  /* Open EP IN */
  DCD_EP_Open(pdev,
              HID_IN_EP,
              HID_IN_PACKET,
              USB_EP_INT);
  
  /* Open EP OUT */
  DCD_EP_Open(pdev,
              HID_OUT_EP,
              HID_OUT_PACKET,
              USB_EP_INT);
 
  /*Receive Data*/
  DCD_EP_PrepareRx(pdev,HID_OUT_EP,Report_buf,64); //??DCD_EP_PrepareRx(pdev,HID_OUT_EP,Report_buf,135);//65
  
  return USBD_OK;
}

/**
  * @brief  USBD_HID_Init
  *         DeInitialize the HID layer
  * @param  pdev: device instance
  * @param  cfgidx: Configuration index
  * @retval status
  */
uint8_t  USBD_HID_DeInit (void  *pdev, 
                                 uint8_t cfgidx)
{
  /* Close HID EPs */
  DCD_EP_Close (pdev , HID_IN_EP);
  DCD_EP_Close (pdev , HID_OUT_EP);
  
  return USBD_OK;
}

/**
  * @brief  USBD_HID_Setup
  *         Handle the HID specific requests
  * @param  pdev: instance
  * @param  req: usb requests
  * @retval status
  */
uint8_t  USBD_HID_Setup (void  *pdev, 
                                USB_SETUP_REQ *req)
{
  uint8_t USBD_HID_Report_LENGTH=0;
  uint16_t len = 0;
  uint8_t  *pbuf = NULL;

  
  switch (req->bmRequest & USB_REQ_TYPE_MASK)
  {
  case USB_REQ_TYPE_CLASS :  
    switch (req->bRequest)
    {
    case HID_REQ_SET_PROTOCOL:
      USBD_HID_Protocol = (uint8_t)(req->wValue);
      break;
      
    case HID_REQ_GET_PROTOCOL:
      USBD_CtlSendData (pdev, 
                        (uint8_t *)&USBD_HID_Protocol,
                        1);    
      break;
      
    case HID_REQ_SET_IDLE:
      USBD_HID_IdleState = (uint8_t)(req->wValue >> 8);
      break;
      
    case HID_REQ_GET_IDLE:
      USBD_CtlSendData (pdev, 
                        (uint8_t *)&USBD_HID_IdleState,
                        1);        
      break;
      
    case HID_REQ_SET_REPORT:
      flag = 1;
      USBD_HID_Report_ID = (uint8_t)(req->wValue);
      USBD_HID_Report_LENGTH = (uint8_t)(req->wLength);
      USBD_CtlPrepareRx (pdev, Report_buf, USBD_HID_Report_LENGTH);
      
      break;
   
    default:
      USBD_CtlError (pdev, req);
      return USBD_FAIL; 
    }
    break;
    
  case USB_REQ_TYPE_STANDARD:
    switch (req->bRequest)
    {
    case USB_REQ_GET_DESCRIPTOR: 
      if( req->wValue >> 8 == HID_REPORT_DESC)
      {
        len = MIN(CUSTOMHID_SIZ_REPORT_DESC , req->wLength);
        pbuf = (uint8_t*)CustomHID_ReportDescriptor;
      }
      else if( req->wValue >> 8 == HID_DESCRIPTOR_TYPE)
      {
        pbuf = (uint8_t*)USBD_HID_CfgDesc + 0x12;
        len = MIN(USB_HID_DESC_SIZ , req->wLength);
      }
      
      USBD_CtlSendData (pdev, 
                        pbuf,
                        len);
      
      break;
      
    case USB_REQ_GET_INTERFACE :
      USBD_CtlSendData (pdev,
                        (uint8_t *)&USBD_HID_AltSet,
                        1);
      break;
      
    case USB_REQ_SET_INTERFACE :
      USBD_HID_AltSet = (uint8_t)(req->wValue);
      break;
    }
  }
  return USBD_OK;
}

/**
  * @brief  USBD_HID_SendReport 
  *         Send HID Report
  * @param  pdev: device instance
  * @param  buff: pointer to report
  * @retval status
  */
uint8_t USBD_HID_SendReport     (USB_CORE_HANDLE  *pdev, 
                                 uint8_t *report,
                                 uint16_t len)
{
  /* Check if USB is configured */
  if (pdev->dev.device_status == USB_CONFIGURED )
  {
    DCD_EP_Tx (pdev, HID_IN_EP, report, len);
  }
  return USBD_OK;
}

/**
  * @brief  USBD_HID_GetCfgDesc 
  *         return configuration descriptor
  * @param  speed : current device speed
  * @param  length : pointer data length
  * @retval pointer to descriptor buffer
  */
uint8_t  *USBD_HID_GetCfgDesc (uint8_t speed, uint16_t *length)
{
  *length = sizeof (USBD_HID_CfgDesc);
  return (uint8_t*)USBD_HID_CfgDesc;
}

/**
  * @brief  USBD_HID_DataIn
  *         handle data IN Stage
  * @param  pdev: device instance
  * @param  epnum: endpoint index
  * @retval status
  */
uint8_t  USBD_HID_DataIn (void  *pdev, 
                                 uint8_t epnum)
{
  if (epnum == 1) PrevXferDone = 1;

  return USBD_OK;
}

/**
  * @brief  USBD_HID_DataOut
  *         handle data IN Stage
  * @param  pdev: device instance
  * @param  epnum: endpoint index
  * @retval status
  */

void ResetKey(void)
{
  FLASH_Unlock();
  FLASH_ErasePage(BOOTLOADER_KEY_START_ADDRESS);
  FLASH_Lock();
} // End of ResetKey()

void led_off (void);
void led_on (void);
void DelaymS(__IO uint32_t nCount);


void data_answer(void);

extern uint8_t end_time_wait;
extern bool flag_cmdUSB;
//extern uint8_t status_busy;
//void select_range_led(TYPE_meas_led meas, range_test volt);
uint8_t last_cmd = 0; 
uint8_t count_last_cmd = 7;

int tmptmp = 1;

extern uint8_t  corr_coeff[64];
extern	uint16_t change_tmp;
extern	bool UP_DW; 
extern bool comm_INDEC;	
int count_debug_command = 0;

extern bool flag_paket_for_COM;
extern uint8_t data_from_soft[260];
extern uint16_t len_packet_COM;
extern bool flag_read_buf_COM;
extern bool flag_start_sendWIFI;
extern bool debug_Transmit_USB;
//int debag_tmp = 0;
uint32_t flash_read(uint32_t address);
/////////////////////////////////////////////////////////
////////////////////////////////////////////////////////
uint8_t  USBD_HID_DataOut (void  *pdev, uint8_t epnum) {
//	  BitAction Led_State;
//	uint16_t mask;
	uint32_t data_tmp;
//	uint16_t tmp1, tmp2, tmp3, tmp4, tmp5;
//	uint32_t tmp_rcc, adress_tmp;
	/*
extern	uint16_t change_tmp;
extern	bool UP_DW; 
extern bool comm_INDEC;	
*/
//	int i = 6; 
	int	k = 0;
//////////////////////////////////////////////////////////////////
//	TIM_Cmd(TIM3, DISABLE);	
	debug_Transmit_USB = true;
	flag_start_sendWIFI = false;
	count_debug_command++;
	if(Report_buf[5] == erase){
						__disable_irq();
						ResetKey();
						FLASH_Unlock();
						FLASH_ProgramWord(BOOTLOADER_KEY_START_ADDRESS, mode_programm);
						FLASH_Lock();
						NVIC_SystemReset();
	}
	
//////////////////////////////////////////////////////////////////  
			if(status_LED) {led_one_ON(LED_R); status_LED = false; }
			else {led_one_OFF(LED_R); status_LED = true;}
						
			
////////////////////////////////////////
/*			
			if(Report_buf[4]!=0x74 && Report_buf[4]!=0x75 && Report_buf[4]!=0x76){				///?????????????? 0x74, 0x75 ?????? ???????????? ?? ?????? ????????????
			PARAM.count_COMAND_ = Report_buf[3];
			PARAM.COMAND_ = Report_buf[4];

			PARAM.time_NIGHT_ = (int)Report_buf[i]; i++;
			PARAM.time_NIGHT_ = (((int)Report_buf[i])<<8) | PARAM.time_NIGHT_; i++;
			PARAM.time_NIGHT_ = (((int)Report_buf[i])<<16) | PARAM.time_NIGHT_; i++;
			PARAM.time_DAY_ = (int)Report_buf[i]; i++;
			PARAM.time_DAY_ = (((int)Report_buf[i])<<8) | PARAM.time_DAY_; i++;
			PARAM.time_DAY_ = (((int)Report_buf[i])<<16) | PARAM.time_DAY_; i++;
			PARAM.work_GAS = Report_buf[i]; i++;
			PARAM.work_TARIF_ = Report_buf[i]; i++;
			PARAM.set_TMP_ = Report_buf[i]; i++;
			PARAM.SECOND_ = (int)Report_buf[i]; i++;
			PARAM.SECOND_ = (((int)Report_buf[i])<<8) | PARAM.SECOND_; i++;
			PARAM.SECOND_ = (((int)Report_buf[i])<<16) | PARAM.SECOND_; i++;
			PARAM.gisteresis_TMP_ = (uint16_t)Report_buf[i]; i++;
			PARAM.gisteresis_TMP_ = (((uint16_t)Report_buf[i])<<8) | PARAM.gisteresis_TMP_; i++;
			PARAM.gisteresis_TARIF_ = Report_buf[i]; i++;
			PARAM.delta_tmp_ = Report_buf[i]; i++;
			PARAM.sys_DATA[0] = Report_buf[i]; i++;
			PARAM.sys_DATA[1] = Report_buf[i]; i++;
			PARAM.sys_DATA[2] = Report_buf[i]; i++;
			PARAM.sys_DATA[3] = Report_buf[i]; i++;
			
			if(PARAM.count_COMAND_ != count_last_cmd){
				flag_cmdUSB = true;
				out_for_SMART.count_COMAND = PARAM.count_COMAND_;
				out_for_SMART.COMAND = PARAM.COMAND_;
				change_tmp = PARAM.delta_tmp_;
			};
//			flag_cmdUSB = true;
			count_last_cmd = PARAM.count_COMAND_;
			
			if(out_for_SMART.COMAND == cmd_save_kf) save_coeff_cor();
		}
*/
			if(Report_buf[4]==0x74){								// ?????????????? ???? ???????????? ?? ?????? ????????
				len_packet_COM = Report_buf[6];
				for(k = 0; k<len_packet_COM; k++){
					data_from_soft[k]= Report_buf[k+7];
				}
				flag_paket_for_COM = true;
			}
			if(Report_buf[4]==0x75)	{							// ?????????????? ???? ???????????? ???? ???????????? ??????
				flag_read_buf_COM = true;
			}
			if(Report_buf[4]==0x76)	{							// ?????????????? ???? ???????????????? ???????????? ??????????????
				len_packet_COM = Report_buf[6];
				for(k = 0; k<len_packet_COM; k++){
					data_from_soft[k]= Report_buf[k+7];
				}
				flag_start_sendWIFI = true;
			}
			if(Report_buf[4]==0x99)	{							// ???????????????? ???????????? ?? ???????? ?????????????????? ?????????????????????? ????????????????
					data_tmp = flash_read(BOOTLOADER_KEY_START_ADDRESS+4); 
					Send_Buffer[4] = 0x99;
					Send_Buffer[6] = (uint8_t)data_tmp; Send_Buffer[7] = (uint8_t)(data_tmp>>8); Send_Buffer[8] = (uint8_t)(data_tmp>>16); Send_Buffer[9] = (uint8_t)(data_tmp>>24);
					data_tmp = flash_read(BOOTLOADER_KEY_START_ADDRESS+8); 
					Send_Buffer[10] = (uint8_t)data_tmp; Send_Buffer[11] = (uint8_t)(data_tmp>>8); Send_Buffer[12] = (uint8_t)(data_tmp>>16); Send_Buffer[13] = (uint8_t)(data_tmp>>24);
					data_answer();
			}
///////////////////////////////////
			
///////////////////////////////////
  DCD_EP_PrepareRx(pdev,HID_IN_EP,Report_buf,64); //??DCD_EP_PrepareRx(pdev,HID_IN_EP,Report_buf,135);//65
  led_one_OFF(LED_R);
//	if(!flag_paket_for_COM && !flag_read_buf_COM && !flag_start_sendWIFI)TIM_Cmd(TIM3, ENABLE);	 // ???????????????? ???????????? ?????????? ?????????????????? ?????????????? ?????? ?????? ??????????
		if(!flag_paket_for_COM && !flag_read_buf_COM && !flag_start_sendWIFI) {
			debug_Transmit_USB = false;
			flag_start_sendWIFI = true;
		}
		return USBD_OK;
}
////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////
void get_cor(void){
	int i;
	for(i = 0; i<20; i++){
		dataINint[i] = corr_coeff[i];
	}
}

void save_CORR_DEF(void){
	int i, j=0;
//	uint8_t dim_koef_def[20]= {5,12,227,0,137,55,159,0,133,99,46,0,129,226,71,0,2,0,0,0	};
//	uint8_t dim_koef_def[20]= {194,28,124,0,136,41,40,0,132,89,44,0,129,30,218,0,1,217,138,128 };	
//	uint8_t dim_koef_def[20]= {194,98,129,0,136,40,44,0,132,140,54,0,129,119,123,0,1,37,139,128 };	
  
///uint8_t dim_koef_def[20]= {194,   240,   114,    0,    136,    21,    132,    1,    133,    94,    46,    0,    129,    85,    168,    0,    1,    103,    125,    128 };	
//HEX                          c2     f0     72     0      88     15      84     1      85     5e     2e     0      81     55      a8     0     1      67      7d      80
///                            |      |      |      |      |      |       |      |      |      |      |      |      |      |       |      |     |      |       |       |
///         (-AE0<<4)|(znaki koef)    A1     A1     A1    -A1E    A2      A2     A2    -A2E    A3     A3     A3    -A3E    A4      A4     A4    A4E    A0      A0     -A0
/// y = -3,2103E-12x4 + 2,9424E-08x3 - 9,9349E-05x2 + 1,1870E-01x + 4,3093E+01  
///koef(koef*10000) - 	A0 = 0x7d67 (0x80 - minus), A1 = 0x0072f0, A2 = 0x018415, A3 = 0x002e5e, A4 = 0x00a855
/// A0E = 0xc2 >> 4 = 12, A1E = 0x88 & 7f = 8, A2E = 0x85 & 7F = 5, A3E = 0x81 & 0x7f, A4E = 1

/// y = -0,0000000000004614x4 - 0,0000000003850742x3 + 0,0000168923444017x2 - 0,0688235996024750x + 105,8466725843160000
//A0 = -4,614 E-13
//A1 = -385,0742 E-12
//A2 = 168,9234 E-7
//A3 = -688,2359 E-4
//A4 = 105,84667
///	A0 = 0xb43c (0x80 - minus), A1 = 0x3AC1F6, A2 = 0x19C692, A3 = 0x690437, A4 = 0x1026A3
/// A0E<<4 = 0xD0, -A1E = 0x8C, -A2E = 0x87, -A3E = 0x84, A4E = 0  
///  uint8_t dim_koef_def[20]= {0xD5, 0xF6, 0xC1, 0x3A, 0x8C, 0x92, 0xC6, 0x19, 0x87, 0x37, 0x04, 0x69, 0x84, 0xA3, 0x26, 0x10, 0, 0x3c, 0xb4, 0x80};

//y = 0,0000000000004902x4 - 0,0000000076952993x3 + 0,0000356336879455x2 - 0,0868252166489503x + 110,8097052886300000
//A0 = 4,902 E-13
//A1 = -769,5299 E-11
//A2 = 356,3369 E-7
//A3 = -868,2521 E-4
//A4 = 110,8097
///	A0 = 0xbf7c , A1 = 0x756BC3, A2 = 0x365F69, A3 = 0x847C19, A4 = 0x10E881    
/// A0E<<4 = 0xD0, -A1E = 0x8C, -A2E = 0x87, -A3E = 0x84, A4E = 0 
 uint8_t dim_koef_def[20]= {0xd5, 0xc3, 0x6b, 0x75, 0x8b, 0x69, 0x5f, 0x36, 0x87, 0x19, 0x7c, 0x84, 0x84, 0x81, 0xe8, 0x10, 0, 0x7c, 0xbf, 0};    
   
	for(i = 0; i<256; i++){															// ?????????????????? ???????????? ????????. ?????????????????? ???? ?????????? ?? ???????????? buf_COEFF[i]
		buf_COEFF[i] = flash_read(ADDRESS_KORR_COEFF+j);
		j+=4;
	}
	j = 0;
	for(i = 0; i<5; i++){																// ???????????????????????? ???????????? ?????? ??????????
			buf_COEFF[i] = (uint32_t)(dim_koef_def[j]) | ((uint32_t)(dim_koef_def[1+j])<<8) | 
			((uint32_t)(dim_koef_def[2+j])<<16) | ((uint32_t)(dim_koef_def[3+j])<<24);
			j+=4;
	}
//	buf_COEFF[2] = (int)(Report_buf[16]);
////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////	?????????????? ???????????????? ?? ????????????????????????????
		
	__disable_irq();	
	FLASH_Unlock();
  FLASH_ErasePage(ADDRESS_KORR_COEFF);
  FLASH_Lock();
	DelaymS(1);
////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////// ???????????????? ???????????????? ?? ???????????????????????????? ???? ?????????????? buf_COEFF[i]
	FLASH_Unlock();
	j=0;
	
	for(i = 0; i<256; i++){
		FLASH_ProgramWord(ADDRESS_KORR_COEFF+j, buf_COEFF[i]); //
		j+=4;
	}
	
	FLASH_Lock();
	__enable_irq();
////////////////////////////////////////////////////////////////
//	Send_Buffer[17] = status_OK;	
//	data_answer();
	DelaymS(30);
}
void read_FLASH_CORR(void){
	uint32_t data, adress;
	int shift=0;
	int i, jk=0;
//	shift = (int)(Report_buf[50])*4;
	adress = shift + ADDRESS_KORR_COEFF;
	if(flash_read(adress) == 0xffffffff){ save_CORR_DEF(); };
	for(i = 0; i<5; i++){
		data = flash_read(adress+jk);   //
		/*
						Send_Buffer[8+jk] = (uint8_t)data;
						Send_Buffer[9+jk] = (uint8_t)(data>>8);
						Send_Buffer[10+jk] = (uint8_t)(data>>16);
						Send_Buffer[11+jk] = (uint8_t)(data>>24);
		*/
						corr_coeff[0+jk] = (uint8_t)data;
						corr_coeff[1+jk] = (uint8_t)(data>>8);
						corr_coeff[2+jk] = (uint8_t)(data>>16);
						corr_coeff[3+jk] = (uint8_t)(data>>24);
		
		jk+=4;
	}
//	data = flash_read(adress+8);
//	Send_Buffer[16] = (uint8_t)data;
	get_cor();
}
void save_coeff_cor(uint8_t dim[]){
	int i, j=0;
	
	for(i = 0; i<256; i++){															// ?????????????????? ???????????? ????????. ?????????????????? ???? ?????????? ?? ???????????? buf_COEFF[i]
		buf_COEFF[i] = flash_read(ADDRESS_KORR_COEFF+j);
		j+=4;
	}
	j = 0;
	for(i = 0; i<5; i++){																// ???????????????????????? ???????????? ?????? ??????????
			buf_COEFF[i] = (uint32_t)(dim[29+j+64]) | ((uint32_t)(dim[30+j+64])<<8) | 
			((uint32_t)(dim[31+j+64])<<16) | ((uint32_t)(dim[32+j+64])<<24);
			j+=4;
	}
//	buf_COEFF[2] = (int)(Report_buf[16]);
////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////	?????????????? ???????????????? ?? ????????????????????????????
		
	__disable_irq();	
	FLASH_Unlock();
  FLASH_ErasePage(ADDRESS_KORR_COEFF);
  FLASH_Lock();
	DelaymS(1);
////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////// ???????????????? ???????????????? ?? ???????????????????????????? ???? ?????????????? buf_COEFF[i]
	FLASH_Unlock();
	j=0;
	
	for(i = 0; i<256; i++){
		FLASH_ProgramWord(ADDRESS_KORR_COEFF+j, buf_COEFF[i]); //
		j+=4;
	}
	
	FLASH_Lock();
	__enable_irq();
////////////////////////////////////////////////////////////////
//	Send_Buffer[17] = status_OK;	
//	data_answer();
	DelaymS(30);
	read_FLASH_CORR();
}


uint8_t USBD_HID_EP0_RxReady(void *pdev)
{
  return USBD_OK;
}

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
