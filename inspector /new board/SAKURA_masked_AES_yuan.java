package perturbation;

/** Sequence for Pinata board for encryption with sw DES */
/** (c) Riscure 2016 */
/** Code version: 1.1 */

/**
 *  ChangeLog:
 *  - v1.1, 20161026: Use Sequence data generator support for input data generation
 *  - v1.0, 20150227: Initial release
 */

import static acquisition2.target.CommandLogging.*;
import static acquisition2.target.ErrorHandling.*;
import static com.riscure.signalanalysis.data.SimpleVerdict.*;
import static com.riscure.util.HexUtils.*;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import com.riscure.hardware.raw.RawIODevice;
import com.riscure.hardware.resetline.ResetLineDevice;

import com.riscure.osgi.annotation.Reference;
import com.riscure.beans.annotation.DisplayName;
import acquisition2.target.BasicSequence;
import org.osgi.framework.ServiceReference;
import com.riscure.util.HexUtils;
import java.math.BigInteger;

public class SAKURA_masked_AES_yuan extends BasicSequence {
    private static final String DEFAULT_SAKURA_AES_KEY = "CA FE BA BE DE AD BE EF DE AD BE EF 00 00 00 00";
    private static final int DES_BIT_LENTH = 64;
    private static final int SIZE_OF_BYTE = 8;
    public static final int READ_TIMEOUT_MS = 2000;

    private PinataSequenceSettings settings = new PinataSequenceSettings();

    private RawIODevice rawIODevice1;
    private ResetLineDevice resetLineDevice1;

    @Override
    protected void init() {
        // Get all devices from hardware manager
        rawIODevice1 = getRawIODevice(settings.getRawIODevice1());
        resetLineDevice1 = getResetLineDevice(settings.getResetLineDevice1()); //Yuan: add back the reset line devices
        
        setDefaultDevice(rawIODevice1);
        //close(resetLineDevice1);//y        
        
        setDefaultDevice(resetLineDevice1); //Yuan: add back the device line devices

        // If any error occur the sequence will fail
        onError(FAIL);

        // Open all devices
        open(rawIODevice1);
        //open(resetLineDevice1);//y
        //setProperty(rawIODevice1, "baudrate", 115200); //Yuan: for normal communication, Conor's setting
        setProperty(rawIODevice1, "baudrate", 14400); //Yuan: for 4MHz communication with RISC-V Sakura
        connect(rawIODevice1);
        
        //try{
            
        //}
       //catch(java.lang.RuntimeException e){
       //    close(resetLineDevice1);  
        //    open(resetLineDevice1);//y  
        //}

        open(resetLineDevice1);//y
        send_command(1, hex(DEFAULT_SAKURA_AES_KEY));
        
        /////////////////////////////////////////////////////////////////////////////////////////
        //  Command5,6: commend to SAKURA for mask or unmask options
        ////////////////////////////////////////////////////////////////////////////////////////
        if (this.settings.maskingEnabled)
        {
            send_command(6, null);
        }
        else
        {
            send_command(5, null);
        }
        
    }
    
    private byte[] send_command(int opcode, byte[] payload)
    {
        byte[] command = {(byte) opcode};
        byte[] zeros = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,};

        write(rawIODevice1, command, NO_LOG);
        
        if (payload == null)
            write(rawIODevice1, zeros, NO_LOG);
        else
            write(rawIODevice1, payload, NO_LOG);
        
        byte[] response = readAll(rawIODevice1, 17, READ_TIMEOUT_MS, 0);
        
        if (response.length != 17)
        {
            System.out.println("Board did not respond to command...");   
            resetBoard(); // Yuan: reset the board    
        }
        return response;
    }
    private int runs = 0;
    @Override
    public void run() {
        boolean timeout = false;
        //deassertReset();
        //assertReset();
        
        sleep(100);
        // Set the default verdict to inconclusive
        verdict(INCONCLUSIVE);
        //arm the measurement setup
        arm();
        sleep(20);//Post-arming time delay in ms for the scope to get ready
        // From this point forward ignore errors
        onError(IGNORE);

        if(runs == 0)
            {    
             //   sleep(15000);
            }

        runs++;
        //--------------Yuan: add reset board enable--------------------//
        if(settings.getResetBoardEnabled()){
            //assertReset();
            //sleep(10);
           // deassertReset();
            resetBoard();
        }
        //----------------------------------------------------------------//
        int aesBlockSize = 16;

        byte[] inputKey = hex(DEFAULT_SAKURA_AES_KEY);
        byte[] message = getGeneratorData(aesBlockSize); // get data from the chosen data generator
        byte[] expectedCipherText = crypto.AES.aes_ecb(inputKey, message, 128, 128, true);
        
        /////////////////////////////////////////////////////////////////////////////////////////
        //  Command1: Send the Key to sakura
        ////////////////////////////////////////////////////////////////////////////////////////

        send_command(1, hex(DEFAULT_SAKURA_AES_KEY));
        byte[] command_set_pt = {(byte) 0};
        write(rawIODevice1, command_set_pt, NO_LOG);
        write(rawIODevice1, message, NO_LOG);

        byte[] response = readAll(rawIODevice1, 17, READ_TIMEOUT_MS, 0);
        
        if (response.length != 17 || response[0] != 4)
        {
            System.out.println("Board did not respond to set pt command..."); 
            resetBoard();
                        
        }
        
          /////////////////////////////////////////////////////////////////////////////////////////
        //  Command7: Send command to print out hello worlf for instruction debug
        ////////////////////////////////////////////////////////////////////////////////////////
        byte[] test_fault = send_command(7, null);
        
        
        /////////////////////////////////////////////////////////////////////////////////////////
        //  Command2: Tell the sakura to start runnign the AES
        ////////////////////////////////////////////////////////////////////////////////////////

        byte[] command_run = {(byte) 2};
        
        write(rawIODevice1, command_run, NO_LOG);
        write(rawIODevice1, message, NO_LOG);

        response = readAll(rawIODevice1, 17, READ_TIMEOUT_MS, 0);
        
        if (response.length < 17 )
        {
            System.out.println("Board did not respond to run command...");   
            resetBoard();            
        }
        

        //soft-trigger the measurement setup
        softTrigger();
        addDataIn(message);
        if (response.length > 0)
            {
        addDataOut(Arrays.copyOfRange(response, 1, 17));
        appendLog(message);
        appendLog(response);
        //setLogProperty("Distance", checkDistance(response, expectedCipherText));

        //Verdict setting: board should reply 8 bytes response; if we send the fixed message, check that response is ok
 if (!timeout) {
            //if (checkDistance(response, expectedCipherText) == 8)
           // if (HexUtils.startsWith(Arrays.copyOfRange(response, 1, 17), expectedCipherText)) {
            if (!HexUtils.startsWith(Arrays.copyOfRange(test_fault, 1, 17), hex(DEFAULT_SAKURA_AES_KEY))) {
                //System.out.println("correct");
                verdict(NORMAL);
            } else { //Corrupted ciphertext: desired effect for AES DFA
                verdict(SUCCESSFUL);
                //System.out.println("wrong");
            }
            } else {
            //Yuan: add the reset for the board reseting
            
            resetBoard();
        }
 }    
 } 
 
   private void resetBoard() {
        System.out.println("Board not responding... Resetting...");
       // deassertReset();
        assertReset();
        sleep(10);
        deassertReset();
        //assertReset();
        System.out.println("Reset complete.");
    }

    
    private void custom() {
        System.out.println("Board not responding... Resetting...");
        assertReset();
        sleep(10);
        deassertReset();
        System.out.println("Reset complete.");
    }
    
    private int checkDistance(byte[] a, byte[] b) {
        int distance = 0;
        BigInteger bia = new BigInteger(a);
        BigInteger bib = new BigInteger(b);
        BigInteger tmp = bia.xor(bib);
        return tmp.bitCount();
    }
    
    @Override
    protected void onError(Throwable t) throws RuntimeException {
        if (t instanceof TimeoutException) {
            // Ignore
        } else {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void close() {
        // Close all devices, note that closing does not mean powering down
        close(rawIODevice1);
        close(resetLineDevice1);

    }

    @Override
    public PinataSequenceSettings getSettingsBean() {
        return settings;
    }

    @Override
    public void setSettingsBean(Object settings) {
        this.settings = (PinataSequenceSettings) settings;
    }

    @Override
    public boolean isDataGeneratorEnabled() {
        return true;
    }

    public static class PinataSequenceSettings {

        @Reference(RawIODevice.class)
        @DisplayName("Sakura Board")
        private ServiceReference rawIODevice1;

        public ServiceReference getRawIODevice1() {
            return rawIODevice1;
        }

        public void setRawIODevice1(ServiceReference rawIODevice1) {
            this.rawIODevice1 = rawIODevice1;
        }
 
        @DisplayName("Enable masking")
         private boolean maskingEnabled = false;
        
        public boolean getMaskingEnabled() {
            return maskingEnabled;
        }
        public void setMaskingEnabled(boolean r) {
            this.maskingEnabled = r;
         }
        //-------------------------------yuan------------------
       @Reference(ResetLineDevice.class)
       @DisplayName("Reset Device")
      private ServiceReference resetLineDevice1;

        public ServiceReference getResetLineDevice1() {
            return resetLineDevice1;
        }

        public void setResetLineDevice1(ServiceReference resetLineDevice1) {
            this.resetLineDevice1 = resetLineDevice1;
        }
        //--------------------------------------------------------------
        @DisplayName("Reset board before encryption")
    private boolean resetBoardEnabled = false;
    
    public boolean getResetBoardEnabled() {
        return resetBoardEnabled;
    }
    public void setResetBoardEnabled(boolean resetBoardEnabled) {
        this.resetBoardEnabled = resetBoardEnabled;
    }
    }
    
    private String string(byte[] response) {
        StringBuilder st = new StringBuilder();
        for (int k = 0; k < response.length; k++) {
            st.append(String.format("%02X ", response[k]));
        }
        return String.format("%s", st);
    }
}
