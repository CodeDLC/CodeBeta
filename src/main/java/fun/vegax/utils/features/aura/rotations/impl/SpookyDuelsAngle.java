package fun.vegax.utils.features.aura.rotations.impl;

import fun.vegax.utils.features.aura.rotations.constructor.RotateConstructor;
import fun.vegax.utils.features.aura.warp.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import fun.vegax.features.impl.combat.Aura;
import fun.vegax.VegaXDLC;
import fun.vegax.utils.features.aura.utils.MathAngle;
import java.security.SecureRandom;
import java.util.LinkedList;

public class SpookyDuelsAngle extends RotateConstructor {
    private static final SecureRandom R = new SecureRandom();

    private float baseSpookyDuelseed = 25.5f, fastSpookyDuelseed = 44.5f, shakeIntensity = 2.2f;
    private final LinkedList<Float> yawHist = new LinkedList<>(), pitchHist = new LinkedList<>();
    private int histSize = 3;
    private long lastHit = 0, lastPause = 0, lastBlink = 0, lastSaccade = 0, lastTremor = 0, lastBreath = 0, lastMicro = 0, lastPrediction = 0;
    private int combo = 0, miss = 0, focus = 85;
    private float fatigue = 0, wanderYaw = 0, wanderPitch = 0, errorYaw = 0, errorPitch = 0;
    private float panicYaw = 0, panicPitch = 0, aimDrift = 0, headNoise = 0;
    private float tremorYaw = 0, tremorPitch = 0, breath = 0, microYaw = 0, microPitch = 0;
    private float predictedYaw = 0, predictedPitch = 0;
    private float lastTargetYaw = 0, lastTargetPitch = 0;
    private float[] yawVel = new float[3];
    private int velIdx = 0;
    private float[] bezierBuf = new float[8];
    private int bezierIdx = 0;
    private float avgReaction = 50f;
    private float[] reactionTimes = new float[15];
    private int reactIdx = 0;
    private boolean panicking = false, tired = false, blinking = false;
    private long blinkStart = 0;
    private final float[] randCache = new float[64];
    private int randIdx = 0;

    public SpookyDuelsAngle() {
        super("SpookyDuelsookyTime");
        for(int i=0;i<randCache.length;i++) randCache[i]=R.nextFloat();
        for(int i=0;i<reactionTimes.length;i++) reactionTimes[i]=40+R.nextFloat()*30;
    }

    private float rnd(float min,float max){randIdx=(randIdx+1)&63;return min+randCache[randIdx]*(max-min);}
    private float rnd(){randIdx=(randIdx+1)&63;return randCache[randIdx];}

    private float lerp(float t,float a,float b){return a+t*(b-a);}
    private float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}

    private void predict(Turns target,long time){
        float dt=Math.min(50,time-lastPrediction)/1000f;
        if(lastPrediction>0&&dt>0&&dt<0.1f){
            float yawV=(target.getYaw()-lastTargetYaw)/dt;
            float pitchV=(target.getPitch()-lastTargetPitch)/dt;
            yawVel[velIdx]=yawV;velIdx=(velIdx+1)%3;
            float avgYawV=(yawVel[0]+yawVel[1]+yawVel[2])/3;
            float predTime=0.04f+rnd()*0.03f;
            predictedYaw=target.getYaw()+avgYawV*predTime;
            predictedPitch=target.getPitch()+pitchV*predTime;
        }else{predictedYaw=target.getYaw();predictedPitch=target.getPitch();}
        lastTargetYaw=target.getYaw();lastTargetPitch=target.getPitch();
        lastPrediction=time;
    }
    private float bezier(float val){
        bezierBuf[bezierIdx]=val;bezierIdx=(bezierIdx+1)%8;
        if(bezierIdx<3)return val;
        float p0=bezierBuf[(bezierIdx-3+8)%8],p1=bezierBuf[(bezierIdx-2+8)%8],p2=bezierBuf[(bezierIdx-1+8)%8],p3=val,t=0.3f,mt=1-t;
        return mt*mt*mt*p0+3*mt*mt*t*p1+3*mt*t*t*p2+t*t*t*p3;
    }

    private void humanFactors(long time,boolean canHit){
        if(time-lastHit<100&&combo>2){
            reactionTimes[reactIdx]=Math.max(35f,avgReaction-1f);
            reactIdx=(reactIdx+1)%reactionTimes.length;
            float s=0;for(float r:reactionTimes)s+=r;avgReaction=s/reactionTimes.length;
        }
        if(time-lastTremor>20){lastTremor=time;
            float ti=0.06f*(1f+fatigue*0.5f);
            tremorYaw=(float)(Math.sin(time/45.0)*0.05+Math.sin(time/23.0)*0.03)*ti;
            tremorPitch=(float)(Math.cos(time/48.0)*0.04+Math.sin(time/27.0)*0.02)*ti;
        }
        if(time-lastBreath>100){lastBreath=time;
            breath=(float)(Math.sin(time/2800.0)*0.04+Math.sin(time/1400.0)*0.02);
        }
        if(time-lastMicro>150+rnd(0,200)){lastMicro=time;
            microYaw=rnd(-0.3f,0.3f);microPitch=rnd(-0.2f,0.2f);
        }
        microYaw*=0.92f;microPitch*=0.92f;
        if(time-lastBlink>200){lastBlink=time;headNoise=rnd(-0.25f,0.25f);}
        headNoise*=0.95f;
        if(blinking&&time-blinkStart>100)blinking=false;
        else if(!blinking&&time-lastBlink>3500&&rnd()<0.005f){blinking=true;blinkStart=time;}
        if(time-lastPause>5000){
            if(canHit)focus=Math.max(30,focus-R.nextInt(10));
            else focus=Math.min(100,focus+R.nextInt(15));
            fatigue=Math.min(1f,fatigue+(canHit?0.02f:-0.01f));
            lastPause=time;
        }
        aimDrift=lerp(0.99f,aimDrift,rnd(-1.2f,1.2f));
        float ws=(canHit?0.5f:1.2f)*(1f+fatigue*0.7f);
        wanderYaw=lerp(0.35f,wanderYaw,rnd(-0.8f,0.8f)*ws);
        wanderPitch=lerp(0.35f,wanderPitch,rnd(-0.5f,0.5f)*ws);
    }

    private float microJitter(float amp,long time,boolean yaw){
        double phase=time/(yaw?70:90);
        return (float)(Math.sin(phase)*amp*0.55+Math.sin(phase*2.3)*amp*0.25+Math.sin(phase*4.8)*amp*0.1);
    }
    private float ease(float delta,float maxSpookyDuelseed){
        if(Math.abs(delta)<0.02f)return 0;
        float sign=Math.signum(delta),abs=Math.abs(delta),clamped=Math.min(abs,maxSpookyDuelseed),t=clamped/Math.max(abs,1f);
        return sign*clamped*(1f-(float)Math.pow(1f-t,1.85f));
    }
    private float smooth(float val,LinkedList<Float> hist){
        hist.addLast(val);
        while(hist.size()>histSize)hist.removeFirst();
        if(hist.isEmpty())return val;
        float s=0,w=0;int i=0;
        for(float v:hist){float ww=(float)Math.pow(1.15f,i++);s+=v*ww;w+=ww;}
        return s/w;
    }
    @Override
    public Turns limitAngleChange(Turns current, Turns target, Vec3d vec, Entity entity) {
        long time=System.currentTimeMillis();
        boolean canHit=entity!=null&&VegaXDLC.getInstance().getAttackPerpetrator().getAttackHandler().canAttack(Aura.getInstance().getConfig(),0);

        predict(target,time);
        humanFactors(time,canHit);

        Turns predTarget=new Turns(predictedYaw,predictedPitch);
        Turns delta=MathAngle.calculateDelta(current,predTarget);
        float yawD=delta.getYaw(),pitchD=delta.getPitch(),total=(float)Math.hypot(yawD,pitchD);

        if(canHit){
            if(total<6f){combo++;miss=Math.max(0,miss-1);lastHit=time;}
            else miss++;
        }

        if((miss>8&&time-lastHit>800)||(rnd()<0.008f&&!canHit)){
            if(miss>8){miss=0;}
            return current;
        }
        if(blinking&&rnd()<0.5f)return current;

        float fatigueM=1f-fatigue*0.35f,focusM=focus/100f,missM=miss>15?0.65f:miss>8?0.8f:miss>4?0.9f:1f;
        float reactionM=avgReaction/50f;
        float yawSpookyDuelseed= (Math.abs(yawD)>32?fastSpookyDuelseed:baseSpookyDuelseed) * fatigueM * missM * focusM * reactionM;
        float pitchSpookyDuelseed=(Math.abs(pitchD)>14?fastSpookyDuelseed*0.7f:baseSpookyDuelseed*0.7f) * fatigueM * missM * focusM * reactionM;
        float yawStep=ease(yawD,yawSpookyDuelseed);
        float pitchStep=ease(pitchD,pitchSpookyDuelseed);
        float inacc=(100-focus)/100f*0.8f+fatigue*0.5f;
        float targetOffY=(rnd(-1.2f,1.2f)*0.97f)*(1f-fatigue*0.4f);
        float targetOffP=(rnd(-0.6f,0.6f)*0.97f)*(1f-fatigue*0.4f);
        float jitterY=microJitter(1.5f+fatigue*1.1f,time,true)+rnd(-inacc,inacc)+targetOffY+aimDrift+headNoise+tremorYaw*0.3f+breath*0.2f+microYaw;
        float jitterP=microJitter(0.7f+fatigue*1.1f,time,false)+rnd(-inacc*0.6f,inacc*0.6f)+targetOffP+aimDrift*0.5f+tremorPitch*0.3f+breath*0.15f+microPitch;
        yawStep=smooth(yawStep,yawHist);
        pitchStep=smooth(pitchStep,pitchHist);

        if(canHit&&total<10f&&rnd()<0.25f){
            float over=0.1f*(1f-Math.min(total/10f,1f));
            yawStep*=1f+over;pitchStep*=1f+over;
        }

        float mouseNoise=(float)Math.sin(time/150.0)*0.6f*(Math.abs(yawD)+Math.abs(pitchD))/25f;
        yawStep+=errorYaw+wanderYaw+mouseNoise;
        pitchStep+=errorPitch+wanderPitch+mouseNoise*0.7f;
        yawStep=clamp(yawStep,-50f,50f);
        pitchStep=clamp(pitchStep,-30f,30f);

        float finalYaw = current.getYaw() + yawStep + jitterY;
        float finalPitch = clamp(current.getPitch() + pitchStep + jitterP, -90f, 90f);
        float yawDiff=finalYaw-current.getYaw();
        if(Math.abs(yawDiff)>30f)finalYaw=current.getYaw()+Math.signum(yawDiff)*30f;

        finalYaw=bezier(finalYaw);

        return new Turns(finalYaw,finalPitch).adjustSensitivity();
    }
    @Override
    public Vec3d randomValue(){
        return new Vec3d(rnd(-0.08f,0.08f),rnd(-0.05f,0.05f),rnd(-0.08f,0.08f));
    }
}