package fun.vegax;

import antidaunleak.api.annotation.Native;
import antidaunleak.api.UserProfile;
import fun.vegax.commands.manager.CommandRepository;
import fun.vegax.utils.client.managers.file.exception.FileProcessingException;
import fun.vegax.utils.client.logs.Logger;
import fun.vegax.utils.connection.auracheckft.FTCheckClient;
import fun.vegax.utils.connection.tps.TPSCalculate;
import fun.vegax.utils.display.scissor.ScissorAssist;
import fun.vegax.utils.client.webhook.WebhookManager;
import net.fabricmc.api.ModInitializer;
import fun.vegax.common.repository.box.BoxESPRepository;
import fun.vegax.common.repository.rct.RCTRepository;
import fun.vegax.common.repository.way.WayRepository;
import fun.vegax.common.discord.DiscordManager;
import fun.vegax.utils.client.managers.api.draggable.DraggableRepository;
import fun.vegax.utils.client.managers.file.*;
import fun.vegax.common.repository.macro.MacroRepository;
import fun.vegax.utils.client.managers.event.EventManager;
import fun.vegax.features.module.ModuleProvider;
import fun.vegax.features.module.ModuleRepository;
import fun.vegax.features.module.ModuleSwitcher;
import fun.vegax.utils.client.sound.SoundManager;
import fun.vegax.display.screens.clickgui.MenuScreen;
import fun.vegax.utils.connection.cloud.CloudConfigWebSocketClient;
import fun.vegax.main.client.ClientInfo;
import fun.vegax.main.client.ClientInfoProvider;
import fun.vegax.main.listener.ListenerRepository;
import fun.vegax.commands.CommandDispatcher;
import fun.vegax.utils.features.aura.striking.StrikerConstructor;
import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import fun.vegax.display.screens.mainmenu.altscreen.impl.AccountRepository;
import fun.vegax.utils.client.managers.file.impl.AccountFile;
import fun.vegax.display.screens.mainmenu.altscreen.impl.Account;
import fun.vegax.mixins.client.IMinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import com.mojang.authlib.minecraft.UserApiService;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VegaXDLC implements ModInitializer {
    @Getter
    static VegaXDLC instance;
    EventManager eventManager = new EventManager();
    EventBus eventBus = new EventBus();
    ModuleRepository moduleRepository;
    ModuleSwitcher moduleSwitcher;
    CommandRepository commandRepository;
    CommandDispatcher commandDispatcher;
    BoxESPRepository boxESPRepository = new BoxESPRepository(eventManager);
    MacroRepository macroRepository = new MacroRepository(eventManager);
    WayRepository wayRepository = new WayRepository(eventManager);
    RCTRepository RCTRepository = new RCTRepository(eventManager);
    ModuleProvider moduleProvider;
    DraggableRepository draggableRepository;
    DiscordManager discordManager;
    FileRepository fileRepository;
    FileController fileController;
    ScissorAssist scissorManager = new ScissorAssist();
    ClientInfoProvider clientInfoProvider;
    ListenerRepository listenerRepository;
    StrikerConstructor attackPerpetrator = new StrikerConstructor();
    CloudConfigWebSocketClient cloudConfigClient;
    FTCheckClient ftCheckClient;
    AccountRepository accountRepository;
    TPSCalculate tpsCalculate;
    MenuScreen MenuScreen;
    boolean initialized;
    ScheduledExecutorService reconnectScheduler;
    boolean reconnecting = false;

    @Override
    @Native(type = Native.Type.VMProtectBeginMutation)
    public void onInitialize() {
        instance = this;
        initClientInfoProvider();
        initModules();
        initDraggable();
        initFileManager();
        initCommands();
        initListeners();
        initDiscordRPC();
        initWebSocketClient();
        initFTCheckClient();
        SoundManager.init();
        loadCurrentAccount();

        MenuScreen menuScreen = new MenuScreen();
        menuScreen.initialize();
        
        MenuScreen MenuScreen = new MenuScreen();
        MenuScreen.initialize();
        this.MenuScreen = MenuScreen;
        
        // Initialize new GUI
        fun.vegax.display.screens.clickgui.newgui.NewMenuScreen newMenuScreen = new fun.vegax.display.screens.clickgui.newgui.NewMenuScreen();
        newMenuScreen.initialize();
        
        initialized = true;

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                sendStartupWebhook();
            } catch (Exception e) {
            }
        }).start();
    }

    private void sendStartupWebhook() {
        try {
            String username = UserProfile.getInstance().profile("username");
            String uid = UserProfile.getInstance().profile("uid");
            String role = UserProfile.getInstance().profile("role");
            String clientName = clientInfoProvider.clientName();

            String discordName = "Not Connected";
            String discordAvatar = "Not Connected";

            if (discordManager != null && discordManager.getInfo() != null) {
                String tempName = discordManager.getInfo().userName();
                String tempAvatar = discordManager.getInfo().avatarUrl();

                if (tempName != null && !tempName.isEmpty() && !tempName.equals("Unknown")) {
                    discordName = tempName;
                }

                if (tempAvatar != null && !tempAvatar.isEmpty()) {
                    discordAvatar = tempAvatar;
                }
            }

            WebhookManager.sendClientStartWebhook(username, uid, role, discordName, discordAvatar, clientName);
        } catch (Exception e) {
            Logger.error("Failed to send startup webhook: " + e.getMessage());
        }
    }

    @Native(type = Native.Type.VMProtectBeginMutation)
    private void loadCurrentAccount() {
        if (accountRepository.currentAccount != null && !accountRepository.currentAccount.isEmpty()) {
            Account currentAcc = accountRepository.accountList.stream()
                    .filter(acc -> acc.name.equals(accountRepository.currentAccount))
                    .findFirst()
                    .orElse(null);

            if (currentAcc != null) {
                setSession(currentAcc);
            }
        }
    }

    @Native(type = Native.Type.VMProtectBeginMutation)
    private void setSession(Account account) {
        Session newSession = new Session(account.name, UUID.fromString(account.uuid), "0", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
        IMinecraftClient mca = (IMinecraftClient) MinecraftClient.getInstance();
        mca.setSessionT(newSession);
        MinecraftClient.getInstance().getGameProfile().getProperties().clear();
        UserApiService apiService = UserApiService.OFFLINE;
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManagerT(new SocialInteractionsManager(MinecraftClient.getInstance(), apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, newSession, MinecraftClient.getInstance().runDirectory.toPath()));
        mca.setAbuseReportContextT(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
    }

    @Native(type = Native.Type.VMProtectBeginUltra)
    private void initWebSocketClient() {
        try {
            cloudConfigClient = new CloudConfigWebSocketClient(new URI("ws://45.155.205.202:8080"));
            cloudConfigClient.connect();
        } catch (Exception e) {
        }
    }

    @Native(type = Native.Type.VMProtectBeginUltra)
    private void initFTCheckClient() {
        try {
            ftCheckClient = new FTCheckClient(new URI("ws://45.155.205.202:6312"));
            ftCheckClient.connect();
        } catch (Exception e) {
        }
    }

    private void initDraggable() {
        draggableRepository = new DraggableRepository();
        draggableRepository.setup();
    }

    private void initModules() {
        moduleRepository = new ModuleRepository();
        moduleRepository.setup();
        moduleProvider = new ModuleProvider(moduleRepository.modules());
        moduleSwitcher = new ModuleSwitcher(moduleRepository.modules(), eventManager);
    }

    private void initCommands() {
        commandRepository = new CommandRepository();
        commandDispatcher = new CommandDispatcher(eventManager);
    }

    private void initDiscordRPC() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            return;
        }
        discordManager = new DiscordManager();
        discordManager.init();
    }

    private void initClientInfoProvider() {
        File clientDirectory = new File(MinecraftClient.getInstance().runDirectory, "CodeDLC");
        File filesDirectory = new File(clientDirectory, "Files");
        clientInfoProvider = new ClientInfo("CodeDLC Build 1.1", "CodeDev", "Developer", clientDirectory, filesDirectory);
    }

    private void initFileManager() {
        DirectoryCreator directoryCreator = new DirectoryCreator();
        directoryCreator.createDirectories(clientInfoProvider.clientDir(), clientInfoProvider.filesDir());

        File customDir = new File(clientInfoProvider.filesDir(), "Custom");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

        fileRepository = new FileRepository();
        fileRepository.setup(this);
        accountRepository = new AccountRepository();
        fileRepository.getClientFiles().add(new AccountFile(accountRepository));
        fileController = new FileController(fileRepository.getClientFiles(), clientInfoProvider.filesDir());
        try {
            fileController.loadFiles();
        } catch (FileProcessingException e) {
            Logger.error("Failed to load files: " + e.getMessage());
        }
    }

    private void initListeners() {
        listenerRepository = new ListenerRepository();
        listenerRepository.setup();
        tpsCalculate = new TPSCalculate();
    }

    public ScissorAssist getScissorAssist() {
        return scissorManager;
    }
    
    public MenuScreen getMenuScreen() {
        return MenuScreen;
    }

}
