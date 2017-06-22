package perturbation2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import perturbation2.measurementsetup.GlitchSetup; // Class from smartcard GUI, Yuan: add this class for use clock glitch module for VC-glitcher
import acquisition2.measurementsetup.ICWavesSetup;
import acquisition2.measurementsetup.MultiScopeSetup;
import acquisition2.target.SequenceTarget;
import acquisition2.target.TriggeredProtocolTarget;
import perturbation2.measurementsetup.EmbeddedGlitchSetup;// class from embedded settings
import perturbation2.module.GenericPerturbation;
import com.riscure.signalanalysis.ModuleInterface;
import com.riscure.signalanalysis.acquisition.MeasurementSetup;
import com.riscure.signalanalysis.acquisition.Target;
//import perturbation2.measurementsetup.CustomGlitchSetup; //Yuan
//import perturbation2.measurementsetup.YuanGlitchSetup; //Yuan


@SuppressWarnings("serial")
public class Perturbation3 extends GenericPerturbation implements ModuleInterface {
    //public YuanGlitchSetupSettings vcgss = new YuanGlitchSetupSettings();
    
    @Override
    public void initModule() {
        moduleTitle = "Sequence Perturbation with clock glitch";
        prefix = "Sequence Perturbation clk";
        
        moduleDescription = "Performs perturbation on an embedded device using a sequence, an embedded glitcher and a scope";
        helpFile = "doc/manual/modulesPerturbation2.html";
        moduleVersion = "0.1";
        inputRequired = false;
    }

    @Override
    protected List<MeasurementSetup> createPerturbationMeasurementSetups() throws IOException {
        return null;
    }

    @Override
    protected List<MeasurementSetup> createMeasurementSetups() throws IOException {
        List<MeasurementSetup> result = new ArrayList<MeasurementSetup>();
        result.add(new MultiScopeSetup(1));
        result.add(new GlitchSetup(false));
        //result.add(new EmbeddedGlitchSetup());
       // result.add(new CustomGlitchSetup(false)); //Yuan
       // result.add(new YuanGlitchSetup(false));
        result.add(new ICWavesSetup(false));
        return result;
    }

    @Override
    protected Target createTarget() throws IOException {
        return new SequenceTarget();
    }

}
