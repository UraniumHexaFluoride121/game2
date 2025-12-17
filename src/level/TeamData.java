package level;

import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import save.LoadedFromSave;
import singleplayer.card.Card;
import singleplayer.card.CardAttribute;
import unit.Unit;
import unit.action.Action;
import unit.bot.BotHandler;
import unit.bot.BotHandlerData;
import unit.stats.modifiers.types.ActionDependent;
import unit.stats.modifiers.types.ModifierCategory;
import unit.stats.modifiers.types.UnitDependent;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.function.BinaryOperator;

public class TeamData implements Serializable, Writable, LoadedFromSave {
    @Serial
    private static final long serialVersionUID = 1L;

    public final boolean bot;
    public transient BotHandler botHandler = null;
    public BotHandlerData botHandlerData = new BotHandlerData();

    public final PlayerTeam playerTeam;
    public int unitCount = 0;

    public boolean alive = true;

    public float destroyedUnitsHP = 0; //the HP of now destroyed units of this fleet
    public int destroyedEnemiesCount = 0; //the number of enemy units destroyed

    public Point basePos = null;

    public ArrayList<Card> cards = new ArrayList<>();

    public TeamData(boolean bot, PlayerTeam playerTeam) {
        this.bot = bot;
        this.playerTeam = playerTeam;
    }

    public float getCardModifierValue(ModifierCategory category, BinaryOperator<Float> combine) {
        float v = category.identity;
        for (Card card : cards) {
            for (CardAttribute attribute : card.attributes) {
                if (attribute.modifier.hasCategory(category))
                    v = combine.apply(v, attribute.modifier.effect(category));
            }
        }
        return v;
    }

    public float getCardUnitModifier(ModifierCategory category, Unit unit, BinaryOperator<Float> combine) {
        float v = category.identity;
        for (Card card : cards) {
            for (CardAttribute attribute : card.attributes) {
                if (attribute.modifier.hasCategory(category)) {
                    if (!(attribute.modifier instanceof UnitDependent ud) || ud.test(category, unit)) {
                        v = combine.apply(v, attribute.modifier.effect(category));
                    }
                }
            }
        }
        return v;
    }

    public float getCardActionModifier(ModifierCategory category, Unit unit, Action a, BinaryOperator<Float> combine) {
        float v = category.identity;
        for (Card card : cards) {
            for (CardAttribute attribute : card.attributes) {
                if (attribute.modifier.hasCategory(category)) {
                    if (attribute.modifier instanceof ActionDependent ad && ad.test(category, unit, a)) {
                        v = combine.apply(v, attribute.modifier.effect(category));
                    } else if (!(attribute.modifier instanceof UnitDependent ud) || ud.test(category, unit)) {
                        v = combine.apply(v, attribute.modifier.effect(category));
                    }
                }
            }
        }
        return v;
    }

    public TeamData(DataInputStream reader) throws IOException {
        bot = reader.readBoolean();
        playerTeam = PacketReceiver.readEnum(PlayerTeam.class, reader);
        unitCount = reader.readInt();
        alive = reader.readBoolean();
        destroyedUnitsHP = reader.readFloat();
        destroyedEnemiesCount = reader.readInt();
        basePos = PacketReceiver.readPoint(reader);
        if (reader.readBoolean())
            botHandlerData = new BotHandlerData(reader);
        PacketReceiver.readCollection(cards, () -> new Card(reader), reader);
    }

    public TeamData copy() {
        TeamData d = new TeamData(bot, playerTeam);
        d.unitCount = unitCount;
        d.alive = alive;
        d.destroyedUnitsHP = destroyedUnitsHP;
        d.destroyedEnemiesCount = destroyedEnemiesCount;
        d.basePos = basePos;
        d.botHandlerData = botHandlerData.copy();
        d.cards = new ArrayList<>(cards);
        return d;
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        w.writeBoolean(bot);
        PacketWriter.writeEnum(playerTeam, w);
        w.writeInt(unitCount);
        w.writeBoolean(alive);
        w.writeFloat(destroyedUnitsHP);
        w.writeInt(destroyedEnemiesCount);
        PacketWriter.writePoint(basePos, w);
        w.writeBoolean(botHandlerData != null);
        if (botHandlerData != null)
            botHandlerData.write(w);
        PacketWriter.writeCollection(cards, c -> c.write(w), w);
    }

    @Override
    public void load() {

    }
}
