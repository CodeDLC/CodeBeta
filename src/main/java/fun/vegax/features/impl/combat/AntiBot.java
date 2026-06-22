package fun.vegax.features.impl.combat;

import antidaunleak.api.annotation.Native;
import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.Hand;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.SelectSetting;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.utils.client.Instance;
import fun.vegax.events.packet.PacketEvent;
import fun.vegax.events.player.TickEvent;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AntiBot extends Module {

    public static AntiBot getInstance() {
        return Instance.get(AntiBot.class);
    }

    Set<UUID> suspectSet = new HashSet<>();
    static Set<UUID> botSet = new HashSet<>();

    SelectSetting mode = new SelectSetting(
            "Режим",
            "Выберите режим обнаружения ботов"
    )
            .value("Matrix", "ReallyWorld", "CakeWorld")
            .selected("ReallyWorld");


    public AntiBot() {
        super("AntiBot", "Anti Bot", ModuleCategory.COMBAT);
        setup(mode);
    }


    @EventHandler
    public void onPacket(PacketEvent e) {

        switch (e.getPacket()) {

            case PlayerListS2CPacket list ->
                    checkPlayerAfterSpawn(list);

            case PlayerRemoveS2CPacket remove ->
                    removePlayerBecauseLeftServer(remove);

            default -> {}
        }
    }


    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {


        if (!suspectSet.isEmpty()) {

            mc.world.getPlayers().stream()
                    .filter(p -> suspectSet.contains(p.getUuid()))
                    .forEach(this::evaluateSuspectPlayer);
        }


        if (mode.isSelected("Matrix")) {

            matrixMode();

        } else if (mode.isSelected("ReallyWorld")) {

            ReallyWorldMode();

        } else if (mode.isSelected("CakeWorld")) {

            CakeWorldMode();
        }
    }



    private void checkPlayerAfterSpawn(PlayerListS2CPacket packet) {

        packet.getPlayerAdditionEntries().forEach(entry -> {

            GameProfile profile = entry.profile();

            if (profile == null || isRealPlayer(entry, profile))
                return;


            if (isDuplicateProfile(profile)) {

                botSet.add(profile.getId());

            } else {

                suspectSet.add(profile.getId());
            }
        });
    }



    private void removePlayerBecauseLeftServer(PlayerRemoveS2CPacket packet) {

        packet.profileIds().forEach(uuid -> {

            suspectSet.remove(uuid);
            botSet.remove(uuid);

        });
    }



    private boolean isRealPlayer(PlayerListS2CPacket.Entry entry, GameProfile profile) {

        return entry.latency() < 2 ||
                (profile.getProperties() != null &&
                        !profile.getProperties().isEmpty());
    }




    private void evaluateSuspectPlayer(PlayerEntity player) {


        Iterable<ItemStack> armor = null;


        if (!isFullyEquipped(player)) {

            armor = player.getArmorItems();

        }


        if (isFullyEquipped(player) ||
                hasArmorChanged(player, armor)) {

            botSet.add(player.getUuid());
        }


        suspectSet.remove(player.getUuid());
    }






    private void matrixMode() {


        Iterator<UUID> iterator = suspectSet.iterator();


        while (iterator.hasNext()) {


            UUID uuid = iterator.next();


            PlayerEntity entity =
                    mc.world.getPlayerByUuid(uuid);



            if (entity != null) {


                String name =
                        entity.getName().getString();


                boolean nameBot =
                        name.startsWith("CIT-")
                                && !name.contains("NPC")
                                && !name.contains("[ZNPC]");



                int armor = 0;


                for (ItemStack item : entity.getArmorItems()) {

                    if (!item.isEmpty())
                        armor++;

                }


                boolean fullArmor = armor == 4;


                boolean fakeUUID =
                        !entity.getUuid().equals(
                                UUID.nameUUIDFromBytes(
                                        ("OfflinePlayer:" + name).getBytes()
                                )
                        );



                if (fullArmor || nameBot || fakeUUID) {

                    botSet.add(uuid);

                }
            }


            iterator.remove();
        }
    }







    private void ReallyWorldMode() {


        for (PlayerEntity entity : mc.world.getPlayers()) {


            if (!entity.getUuid().equals(
                    UUID.nameUUIDFromBytes(
                            ("OfflinePlayer:" +
                                    entity.getName().getString())
                                    .getBytes()))
                    &&
                    !botSet.contains(entity.getUuid())
                    &&
                    !entity.getName().getString().contains("NPC")
                    &&
                    !entity.getName().getString().startsWith("[ZNPC]")
            ) {


                botSet.add(entity.getUuid());

            }
        }
    }






    private void CakeWorldMode() {


        for (PlayerEntity entity : mc.world.getPlayers()) {


            if (entity == mc.player)
                continue;


            String name =
                    entity.getName().getString();



            if (name.contains("NPC")
                    || name.startsWith("[ZNPC]"))
                continue;



            int armorCount = 0;



            for (ItemStack item : entity.getArmorItems()) {


                if (!item.isEmpty()
                        && item.getItem() instanceof ArmorItem) {

                    armorCount++;

                }
            }



            /*
              CakeWorld проверка:
              игрок без брони = бот
             */


            if (armorCount == 0) {


                botSet.add(entity.getUuid());


            } else {


                botSet.remove(entity.getUuid());

            }
        }
    }






    public boolean isDuplicateProfile(GameProfile profile) {


        return Objects.requireNonNull(mc.getNetworkHandler())
                .getPlayerList()
                .stream()

                .filter(player ->
                        player.getProfile()
                                .getName()
                                .equals(profile.getName())

                        &&
                        !player.getProfile()
                                .getId()
                                .equals(profile.getId())
                )

                .count() == 1;
    }






    public boolean isFullyEquipped(PlayerEntity entity) {


        return IntStream.rangeClosed(0,3)

                .mapToObj(entity.getInventory()::getArmorStack)

                .allMatch(stack ->
                        stack.getItem() instanceof ArmorItem
                                &&
                                !stack.hasEnchantments()
                );
    }






    public boolean hasArmorChanged(PlayerEntity entity,
                                   Iterable<ItemStack> prevArmor) {


        if(prevArmor == null)
            return true;



        List<ItemStack> current =
                StreamSupport.stream(
                        entity.getArmorItems()
                                .spliterator(),
                        false
                ).toList();



        List<ItemStack> previous =
                StreamSupport.stream(
                        prevArmor.spliterator(),
                        false
                ).toList();



        return !current.equals(previous);
    }






    public boolean isBot(PlayerEntity entity) {


        String name =
                entity.getName().getString();



        boolean nameBot =
                name.startsWith("CIT-")
                        &&
                        !name.contains("NPC")
                        &&
                        !name.startsWith("[ZNPC]");



        return nameBot
                || botSet.contains(entity.getUuid());
    }






    public boolean isBot(UUID uuid) {

        return botSet.contains(uuid);

    }





    public boolean isBotU(Entity entity) {


        return !entity.getUuid().equals(

                UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" +
                                entity.getName().getString())
                                .getBytes()

                )

        )

                && entity.isInvisible()

                && !entity.getName()
                .getString()
                .contains("NPC");
    }






    public void reset() {

        suspectSet.clear();
        botSet.clear();

    }





    @Override
    public void deactivate() {

        reset();

        super.deactivate();

    }
}