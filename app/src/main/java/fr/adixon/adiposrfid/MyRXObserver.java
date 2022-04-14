//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fr.adixon.adiposrfid;

import com.rfid.bean.MessageTran;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.rfid.rxobserver.bean.RXOperationTag;
import com.rfid.rxobserver.bean.RXInventoryTag.RXFastSwitchAntInventoryTagEnd;
import com.rfid.rxobserver.bean.RXInventoryTag.RXInventoryTagEnd;
import com.util.StringTool;

import java.util.Arrays;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class MyRXObserver implements Observer {
    private ReaderSetting m_curReaderSetting = ReaderSetting.newInstance();
    private int mOperationTagCount = 0;

    public MyRXObserver() {
    }

    public final void update(Observable o, Object arg) {
        if (arg instanceof MessageTran) {
            this.analyData((MessageTran) arg);
        }

    }

    private void analyData(MessageTran msgTran) {
        if (msgTran.getPacketType() == -96) {
            switch (msgTran.getCmd()) {
                case -128:
                    this.processInventory(msgTran);
                    break;
                case -127:
                    this.processReadTag(msgTran);
                    break;
                case -126:
                    this.processWriteTag(msgTran);
                    break;
                case -125:
                    this.processLockTag(msgTran);
                    break;
                case -124:
                    this.processKillTag(msgTran);
                    break;
                case -123:
                    this.processSetAccessEpcMatch(msgTran);
                    break;
                case -122:
                    this.processGetAccessEpcMatch(msgTran);
                    break;
                case -119:
                    this.processRealTimeInventory(msgTran);
                    break;
                case -118:
                    this.processFastSwitchInventory(msgTran);
                    break;
                case -117:
                    this.processCustomizedSessionTargetInventory(msgTran);
                    break;
                case -116:
                    this.processSetImpinjFastTid(msgTran);
                    break;
                case -115:
                    this.processSetAndSaveImpinjFastTid(msgTran);
                    break;
                case -114:
                    this.processGetImpinjFastTid(msgTran);
                    break;
                case -112:
                    this.processGetInventoryBuffer(msgTran);
                    break;
                case -111:
                    this.processGetAndResetInventoryBuffer(msgTran);
                    break;
                case -110:
                    this.processGetInventoryBufferTagCount(msgTran);
                    break;
                case -109:
                    this.processResetInventoryBuffer(msgTran);
                    break;
                case -104:
                    this.processTagMask(msgTran);
                    break;
                case -80:
                    this.processISO180006BInventory(msgTran);
                    break;
                case -79:
                    this.processISO180006BReadTag(msgTran);
                    break;
                case -78:
                    this.processISO180006BWriteTag(msgTran);
                    break;
                case -77:
                    this.processISO180006BLockTag(msgTran);
                    break;
                case -76:
                    this.processISO180006BQueryLockTag(msgTran);
                    break;
                case 96:
                    this.processReadGpioValue(msgTran);
                    break;
                case 97:
                    this.processWriteGpioValue(msgTran);
                    break;
                case 98:
                    this.processSetAntConnectionDetector(msgTran);
                    break;
                case 99:
                    this.processGetAntConnectionDetector(msgTran);
                    break;
                case 102:
                    this.processSetTemporaryOutputPower(msgTran);
                    break;
                case 103:
                    this.processSetReaderIdentifier(msgTran);
                    break;
                case 104:
                    this.processGetReaderIdentifier(msgTran);
                    break;
                case 105:
                    this.processSetRfLinkProfile(msgTran);
                    break;
                case 106:
                    this.processGetRfLinkProfile(msgTran);
                    break;
                case 112:
                    this.processReset(msgTran);
                    break;
                case 113:
                    this.processSetUartBaudrate(msgTran);
                    break;
                case 114:
                    this.processGetFirmwareVersion(msgTran);
                    break;
                case 115:
                    this.processSetReaderAddress(msgTran);
                    break;
                case 116:
                    this.processSetWorkAntenna(msgTran);
                    break;
                case 117:
                    this.processGetWorkAntenna(msgTran);
                    break;
                case 118:
                    this.processSetOutputPower(msgTran);
                    break;
                case 119:
                    this.processGetOutputPower(msgTran);
                    break;
                case 120:
                    this.processSetFrequencyRegion(msgTran);
                    break;
                case 121:
                    this.processGetFrequencyRegion(msgTran);
                    break;
                case 122:
                    this.processSetBeeperMode(msgTran);
                    break;
                case 123:
                    this.processGetReaderTemperature(msgTran);
                    break;
                case 126:
                    this.processGetRfPortReturnLoss(msgTran);
            }

        }
    }

    private void processSet(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if (btAryData[0] == 16) {
                this.m_curReaderSetting.btReadId = msgTran.getReadId();
                this.refreshSetting(this.m_curReaderSetting);
                this.onExeCMDStatus(btCmd, (byte) 16);
                return;
            }

            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processReset(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processSetUartBaudrate(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetFirmwareVersion(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 2) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btMajor = btAryData[0];
            this.m_curReaderSetting.btMinor = btAryData[1];
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else {
            if (btAryData.length == 1) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            } else {
                this.onExeCMDStatus(btCmd, (byte) 88);
            }

        }
    }

    private void processSetReaderAddress(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processSetWorkAntenna(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if (btAryData[0] == 16) {
                this.m_curReaderSetting.btReadId = msgTran.getReadId();
                this.refreshSetting(this.m_curReaderSetting);
                this.onExeCMDStatus(btCmd, (byte) 16);
                return;
            }

            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processGetWorkAntenna(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if (btAryData[0] == 0 || btAryData[0] == 1 || btAryData[0] == 2 || btAryData[0] == 3) {
                this.m_curReaderSetting.btReadId = msgTran.getReadId();
                this.m_curReaderSetting.btWorkAntenna = btAryData[0];
                this.refreshSetting(this.m_curReaderSetting);
                this.onExeCMDStatus(btCmd, (byte) 16);
                return;
            }

            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processSetOutputPower(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetOutputPower(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length != 4 && btAryData.length != 1) {
            this.onExeCMDStatus(btCmd, (byte) 88);
        } else {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btAryOutputPower = (byte[]) btAryData.clone();
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        }
    }

    private void processSetFrequencyRegion(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetFrequencyRegion(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 3) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btRegion = btAryData[0];
            this.m_curReaderSetting.btFrequencyStart = btAryData[1];
            this.m_curReaderSetting.btFrequencyEnd = btAryData[2];
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else if (btAryData.length == 6) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btRegion = btAryData[0];
            this.m_curReaderSetting.btUserDefineFrequencyInterval = btAryData[1];
            this.m_curReaderSetting.btUserDefineChannelQuantity = btAryData[2];
            this.m_curReaderSetting.nUserDefineStartFrequency = (btAryData[3] & 255) * 256 * 256 + (btAryData[4] & 255) * 256 + (btAryData[5] & 255);
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else {
            if (btAryData.length == 1) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            } else {
                this.onExeCMDStatus(btCmd, (byte) 88);
            }

        }
    }

    private void processSetBeeperMode(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetReaderTemperature(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 2) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btPlusMinus = btAryData[0];
            this.m_curReaderSetting.btTemperature = btAryData[1];
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else {
            if (btAryData.length == 1) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            } else {
                this.onExeCMDStatus(btCmd, (byte) 88);
            }

        }
    }

    private void processReadGpioValue(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 2) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btGpio1Value = btAryData[0];
            this.m_curReaderSetting.btGpio2Value = btAryData[1];
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else {
            if (btAryData.length == 1) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            } else {
                this.onExeCMDStatus(btCmd, (byte) 88);
            }

        }
    }

    private void processWriteGpioValue(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processSetAntConnectionDetector(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetAntConnectionDetector(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btAntDetector = btAryData[0];
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }
    }

    private void processSetTemporaryOutputPower(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processSetReaderIdentifier(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetReaderIdentifier(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 12) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            Arrays.fill(this.m_curReaderSetting.btAryReaderIdentifier, (byte) 0);
            System.arraycopy(btAryData, 0, this.m_curReaderSetting.btAryReaderIdentifier, 0, btAryData.length);
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else {
            if (btAryData.length == 1) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            } else {
                this.onExeCMDStatus(btCmd, (byte) 88);
            }

        }
    }

    private void processSetRfLinkProfile(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetRfLinkProfile(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if ((btAryData[0] & 255) >= 208 && (btAryData[0] & 255) <= 211) {
                this.m_curReaderSetting.btReadId = msgTran.getReadId();
                this.m_curReaderSetting.btRfLinkProfile = btAryData[0];
                this.refreshSetting(this.m_curReaderSetting);
                this.onExeCMDStatus(btCmd, (byte) 16);
                return;
            }

            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processGetRfPortReturnLoss(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.m_curReaderSetting.btReadId = msgTran.getReadId();
            this.m_curReaderSetting.btReturnLoss = btAryData[0];
            this.refreshSetting(this.m_curReaderSetting);
            this.onExeCMDStatus(btCmd, (byte) 16);
        } else {
            if (btAryData.length == 1) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            } else {
                this.onExeCMDStatus(btCmd, (byte) 88);
            }

        }
    }

    private void processInventory(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 9) {
            RXInventoryTagEnd end = new RXInventoryTagEnd();
            end.mCurrentAnt = btAryData[0];
            end.mTagCount = (btAryData[1] & 255) * 256 + (btAryData[2] & 255);
            end.mReadRate = (btAryData[3] & 255) * 256 + (btAryData[4] & 255);
            end.mTotalRead = (btAryData[5] & 255) * 256 * 256 * 256 + (btAryData[6] & 255) * 256 * 256 + (btAryData[7] & 255) * 256 + (btAryData[8] & 255);
            end.cmd = btCmd;
            this.onInventoryTagEnd(end);
        } else {
            if (btAryData.length == 1) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            } else {
                this.onExeCMDStatus(btCmd, (byte) 88);
            }

        }
    }

    private void processReadTag(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            ++this.mOperationTagCount;
            int nLen = btAryData.length;
            int nDataLen = btAryData[nLen - 3] & 255;
            int nEpcLen = (btAryData[2] & 255) - nDataLen - 4;
            String strPC = StringTool.byteArrayToString(btAryData, 3, 2);
            String strEPC = StringTool.byteArrayToString(btAryData, 5, nEpcLen);
            String strCRC = StringTool.byteArrayToString(btAryData, 5 + nEpcLen, 2);
            String strData = StringTool.byteArrayToString(btAryData, 7 + nEpcLen, nDataLen);
            byte btTemp = btAryData[nLen - 2];
            byte btAntId = (byte) ((btTemp & 3) + 1);
            int nReadCount = btAryData[nLen - 1] & 255;
            RXOperationTag tag = new RXOperationTag();
            tag.strPC = strPC;
            tag.strCRC = strCRC;
            tag.strEPC = strEPC;
            tag.strData = strData;
            tag.nDataLen = nDataLen;
            tag.btAntId = btAntId;
            tag.nOperateCount = nReadCount;
            tag.cmd = msgTran.getCmd();
            this.onOperationTag(tag);
            if (this.mOperationTagCount == (btAryData[0] & 255) * 256 + (btAryData[1] & 255)) {
                this.mOperationTagCount = 0;
                this.onOperationTagEnd((btAryData[0] & 255) * 256 + (btAryData[1] & 255));
            }
        }

    }

    private void processWriteTag(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            int nLen = btAryData.length;
            int nEpcLen = (btAryData[2] & 255) - 4;
            if (btAryData[nLen - 3] != 16) {
                this.onExeCMDStatus(btCmd, btAryData[nLen - 3]);
                return;
            }

            ++this.mOperationTagCount;
            String strPC = StringTool.byteArrayToString(btAryData, 3, 2);
            String strEPC = StringTool.byteArrayToString(btAryData, 5, nEpcLen);
            String strCRC = StringTool.byteArrayToString(btAryData, 5 + nEpcLen, 2);
            String strData = StringTool.byteArrayToString(btAryData, 0, btAryData.length);
            byte btTemp = btAryData[nLen - 2];
            byte btAntId = (byte) ((btTemp & 3) + 1);
            int nReadCount = btAryData[nLen - 1] & 255;
            RXOperationTag tag = new RXOperationTag();
            tag.strPC = strPC;
            tag.strCRC = strCRC;
            tag.strEPC = strEPC;
            tag.strData = strData;
            tag.nDataLen = btAryData.length;
            tag.btAntId = btAntId;
            tag.nOperateCount = nReadCount;
            tag.cmd = msgTran.getCmd();
            this.onOperationTag(tag);
            if (this.mOperationTagCount == (btAryData[0] & 255) * 256 + (btAryData[1] & 255)) {
                this.mOperationTagCount = 0;
                this.onOperationTagEnd((btAryData[0] & 255) * 256 + (btAryData[1] & 255));
            }
        }

    }

    private void processLockTag(MessageTran msgTran) {
        this.processWriteTag(msgTran);
    }

    private void processKillTag(MessageTran msgTran) {
        this.processWriteTag(msgTran);
    }

    private void processSetAccessEpcMatch(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetAccessEpcMatch(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if (btAryData[0] == 1) {
                this.onExeCMDStatus(btCmd, (byte) 17);
                return;
            }

            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            if (btAryData[0] == 0) {
                this.m_curReaderSetting.mMatchEpcValue = StringTool.byteArrayToString(btAryData, 2, btAryData[1] & 255);
                this.refreshSetting(this.m_curReaderSetting);
                this.onExeCMDStatus(btCmd, (byte) 16);
                return;
            }

            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processRealTimeInventory(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else if (btAryData.length == 7) {
            RXInventoryTagEnd end = new RXInventoryTagEnd();
            end.mCurrentAnt = btAryData[0];
            end.mReadRate = (btAryData[1] & 255) * 256 + (btAryData[2] & 255);
            end.mTotalRead = (btAryData[3] & 255) * 256 * 256 * 256 + (btAryData[4] & 255) * 256 * 256 + (btAryData[5] & 255) * 256 + (btAryData[6] & 255);
            end.cmd = btCmd;
            this.onInventoryTagEnd(end);
        } else {
            int nLength = btAryData.length;
            int nEpcLength = nLength - 4;
            String strEPC = "";
            if (nEpcLength != 0) {
                strEPC = StringTool.byteArrayToString(btAryData, 3, nEpcLength);
            }

            String strPC = StringTool.byteArrayToString(btAryData, 1, 2);
            String strRSSI = String.valueOf(btAryData[nLength - 1] & 255);
            byte btTemp = btAryData[0];
            byte btAntId = (byte) ((btTemp & 3) + 1);
            byte btFreq = (byte) ((btTemp & 255) >> 2);
            String strFreq = this.getFreqString(btFreq);
            RXInventoryTag tag = new RXInventoryTag();
            tag.strPC = strPC;
            tag.strEPC = strEPC;
            tag.strRSSI = strRSSI;
            tag.strFreq = strFreq;
            tag.btAntId = btAntId;
            tag.cmd = btCmd;
            this.onInventoryTag(tag);
        }

    }

    private void processFastSwitchInventory(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else if (btAryData.length == 2) {
            this.onExeCMDStatus(btCmd, btAryData);
        } else {
            int nSwitchTotal;
            int nSwitchTime;
            if (btAryData.length == 7) {
                nSwitchTotal = (btAryData[0] & 255) * 255 * 255 + (btAryData[1] & 255) * 255 + (btAryData[2] & 255);
                nSwitchTime = (btAryData[3] & 255) * 255 * 255 * 255 + (btAryData[4] & 255) * 255 * 255 + (btAryData[5] & 255) * 255 + (btAryData[6] & 255);
                RXFastSwitchAntInventoryTagEnd tagEnd = new RXFastSwitchAntInventoryTagEnd();
                tagEnd.mTotalRead = nSwitchTotal;
                tagEnd.mCommandDuration = nSwitchTime;
                this.onFastSwitchAntInventoryTagEnd(tagEnd);
            } else {
                nSwitchTotal = btAryData.length;
                nSwitchTime = nSwitchTotal - 4;
                String strEPC = StringTool.byteArrayToString(btAryData, 3, nSwitchTime);
                String strPC = StringTool.byteArrayToString(btAryData, 1, 2);
                String strRSSI = String.valueOf(btAryData[nSwitchTotal - 1] & 255);
                byte btTemp = btAryData[0];
                byte btAntId = (byte) ((btTemp & 3) + 1);
                byte btFreq = (byte) ((btTemp & 255) >> 2);
                String strFreq = this.getFreqString(btFreq);
                RXInventoryTag tag = new RXInventoryTag();
                tag.strPC = strPC;
                tag.strEPC = strEPC;
                tag.strRSSI = strRSSI;
                tag.strFreq = strFreq;
                tag.btAntId = btAntId;
                tag.cmd = btCmd;
                this.onInventoryTag(tag);
            }
        }

    }

    private void processCustomizedSessionTargetInventory(MessageTran msgTran) {
        this.processRealTimeInventory(msgTran);
    }

    private void processSetImpinjFastTid(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processSetAndSaveImpinjFastTid(MessageTran msgTran) {
        this.processSet(msgTran);
    }

    private void processGetImpinjFastTid(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if (btAryData[0] == 0 || (btAryData[0] & 255) == 141) {
                this.m_curReaderSetting.btReadId = msgTran.getReadId();
                this.m_curReaderSetting.btMonzaStatus = btAryData[0];
                this.refreshSetting(this.m_curReaderSetting);
                this.onExeCMDStatus(btCmd, (byte) 16);
                return;
            }

            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processISO180006BInventory(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if ((btAryData[0] & 255) != 255) {
                this.onExeCMDStatus(btCmd, btAryData[0]);
            }
        } else if (btAryData.length == 9) {
            String strUID = StringTool.byteArrayToString(btAryData, 1, 8);
            this.onInventory6BTag(btAryData[0], strUID);
        } else if (btAryData.length == 2) {
            this.onInventory6BTagEnd(btAryData[1] & 255);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processISO180006BReadTag(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            String strData = StringTool.byteArrayToString(btAryData, 1, btAryData.length - 1);
            this.onRead6BTag(btAryData[0], strData);
        }

    }

    private void processISO180006BWriteTag(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onWrite6BTag(btAryData[0], btAryData[1]);
        }

    }

    private void processISO180006BLockTag(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, (byte) 17);
        } else {
            this.onLock6BTag(btAryData[0], btAryData[1]);
        }

    }

    private void processISO180006BQueryLockTag(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onLockQuery6BTag(btAryData[0], btAryData[1]);
        }

    }

    private void processGetInventoryBuffer(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            int nDataLen = btAryData.length;
            int nEpcLen = (btAryData[2] & 255) - 4;
            String strPC = StringTool.byteArrayToString(btAryData, 3, 2);
            String strEPC = StringTool.byteArrayToString(btAryData, 5, nEpcLen);
            String strCRC = StringTool.byteArrayToString(btAryData, 5 + nEpcLen, 2);
            String strRSSI = String.valueOf(btAryData[nDataLen - 3] & 255);
            byte btTemp = btAryData[nDataLen - 2];
            byte btAntId = (byte) ((btTemp & 3) + 1);
            int nReadCount = btAryData[nDataLen - 1] & 255;
            RXInventoryTag tag = new RXInventoryTag();
            tag.strPC = strPC;
            tag.strCRC = strCRC;
            tag.strEPC = strEPC;
            tag.btAntId = btAntId;
            tag.strRSSI = strRSSI;
            tag.mReadCount = nReadCount;
            tag.cmd = btCmd;
            this.onInventoryTag(tag);
        }

    }

    private void processGetAndResetInventoryBuffer(MessageTran msgTran) {
        this.processGetInventoryBuffer(msgTran);
    }

    private void processGetInventoryBufferTagCount(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 2) {
            this.onGetInventoryBufferTagCount((btAryData[0] & 255) * 256 + (btAryData[1] & 255));
        } else if (btAryData.length == 1) {
            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processResetInventoryBuffer(MessageTran msgTran) {
        byte btCmd = msgTran.getCmd();
        byte[] btAryData = msgTran.getAryData();
        if (btAryData.length == 1) {
            if (btAryData[0] == 16) {
                this.onExeCMDStatus(btCmd, (byte) 16);
                return;
            }

            this.onExeCMDStatus(btCmd, btAryData[0]);
        } else {
            this.onExeCMDStatus(btCmd, (byte) 88);
        }

    }

    private void processTagMask(MessageTran msgTran) {
        if (msgTran != null) {
            this.onConfigTagMask(msgTran);
        }

    }

    private String getFreqString(byte btFreq) {
        float nStart;
        if (this.m_curReaderSetting.btRegion == 4) {
            nStart = (float) (btFreq & 255) * (float) (this.m_curReaderSetting.btUserDefineFrequencyInterval & 255) * 10.0F;
            float nstartFrequency = (float) (this.m_curReaderSetting.nUserDefineStartFrequency & 255) / 1000.0F;
            nStart = nstartFrequency + nStart / 1000.0F;
            String strTemp = String.format("%.3f", nStart);
            return strTemp;
        } else {
            String strTemp;
            if ((btFreq & 255) < 7) {
                nStart = 865.0F + (float) (btFreq & 255) * 0.5F;
                strTemp = String.format("%.2f", nStart);
                return strTemp;
            } else {
                nStart = 902.0F + ((float) (btFreq & 255) - 7.0F) * 0.5F;
                strTemp = String.format("%.2f", nStart);
                return strTemp;
            }
        }
    }

    protected void refreshSetting(ReaderSetting readerSetting) {
    }

    protected void onExeCMDStatus(byte cmd, byte status) {
    }

    protected void onExeCMDStatus(byte cmd, byte[] status) {
    }

    protected void onInventoryTag(RXInventoryTag tag) {
    }

    protected void onInventoryTagEnd(RXInventoryTagEnd tagEnd) {
    }

    protected void onFastSwitchAntInventoryTagEnd(RXFastSwitchAntInventoryTagEnd tagEnd) {
    }

    protected void onInventory6BTag(byte nAntID, String strUID) {
    }

    protected void onInventory6BTagEnd(int nTagCount) {
    }

    protected void onRead6BTag(byte antID, String strData) {
    }

    protected void onWrite6BTag(byte nAntID, byte nWriteLen) {
    }

    protected void onLock6BTag(byte nAntID, byte nStatus) {
    }

    protected void onLockQuery6BTag(byte nAntID, byte nStatus) {
    }

    protected void onGetInventoryBufferTagCount(int nTagCount) {
    }

    protected void onOperationTag(RXOperationTag tag) {
    }

    protected void onOperationTagEnd(int operationTagCount) {
    }

    protected void onConfigTagMask(MessageTran msgTran) {
    }
}
