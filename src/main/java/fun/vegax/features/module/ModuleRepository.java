package fun.vegax.features.module;

import fun.vegax.features.impl.combat.*;
import fun.vegax.features.impl.misc.*;
import fun.vegax.features.impl.movement.*;
import fun.vegax.features.impl.player.*;
import fun.vegax.features.impl.render.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleRepository {
    List<Module> modules = new ArrayList<>();

    public void setup() {
        register(
                new AntiAFK(),
                new AutoChestWarden(),
                new JumpCircle(),
                new FakePlayer(),
                new BetterMinecraft(),
                new ProjectileHelper(),
                new TargetStrafe(),
                new FakeMessage(),
                new Strafe(),
                new AutoPotion(),
                new AutoGApple(),
                new AutoPilot(),
                new AirStuck(),
                new Capes(),
                new AutoDuels(),
                new AutoMace(),
                new NoEntityTrace(),
                new ElytraMotion(),
                new LongJump(),
                new ShiftTap(),
                new AspectRatio(),
                new FreeLook(),
                new ClickPearl(),
                new ClickFriend(),
                new TabParser(),
                new WindJump(),
                new HitRange(),
                new TargetESP(),
                new NoWeb(),
                new ServerHelper(),
                new ItemScroller(),
                new Hud(),
                new AuctionHelper(),
                new ProjectilePrediction(),
                new WorldParticles(),
                new ElytraTarget(),
                new XRay(),
                new TriggerBot(),
                new Aura(),
                new AutoSwap(),
                new NoFriendDamage(),
                new HitBoxModule(),
                new AntiBot(),
                new AutoCrystal(),
                new AutoSprint(),
                new Speed(),
                new NoPush(),
                new ElytraHelper(),
                new JoinerHelper(),
                new NoDelay(),
                new Velocity(),
                new AutoRespawn(),
                new NoSlow(),
                new InventoryMove(),
                new Blink(),
                new AutoTool(),
                new Fly(),
                new FastBreak(),
                new CameraSettings(),
                new SwingAnimation(),
                new ViewModel(),
                new BlockOverlay(),
                new AutoTotem(),
                new FastBow(),
                new Esp(),
                new BlockESP(),
                new FreeCam(),
                new ChestStealer(),
                new AutoTpAccept(),
                new Arrows(),
                new AutoLeave(),
                new WorldTweaks(),
                new NoRender(),
                new AutoMessage(),
                new NameProtect(),
                new SelfDestruct(),
                new SeeInvisible(),
                new TargetPearl(),
                new AutoArmor(),
                new AutoUse(),
                new NoInteract(),
                new CrossHair(),
                new ItemFixSwap(),
                new SuperFireWork(),
                new Spider(),
                new ServerRPSpoofer(),
                new KillEffect(),
                new FakeLag(),
                new SantaHatModule(),
                new Trails(),
                new FullBright(),
                new FireFly(),
                new MultiAction()
        );
    }

    
    public void register(Module... module) {
        modules.addAll(List.of(module));
    }

    public List<Module> modules() {
        return modules;
    }
}
