/*
 * Copyright (C) 2013-2014 Byron 3D Games Studio (www.b3dgs.com) Pierre-Alexandre (contact@b3dgs.com)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.b3dgs.warcraft.entity;

import java.util.Collection;

import com.b3dgs.lionengine.core.Core;
import com.b3dgs.lionengine.drawable.Drawable;
import com.b3dgs.lionengine.drawable.Sprite;
import com.b3dgs.lionengine.game.Alterable;
import com.b3dgs.lionengine.game.configurable.Configurable;
import com.b3dgs.lionengine.game.configurable.SurfaceData;
import com.b3dgs.lionengine.game.strategy.ability.skilled.SkilledModel;
import com.b3dgs.lionengine.game.strategy.ability.skilled.SkilledServices;
import com.b3dgs.lionengine.game.strategy.entity.EntityStrategy;
import com.b3dgs.warcraft.AppWarcraft;
import com.b3dgs.warcraft.Player;
import com.b3dgs.warcraft.Race;
import com.b3dgs.warcraft.map.Map;
import com.b3dgs.warcraft.skill.FactorySkill;
import com.b3dgs.warcraft.skill.Skill;

/**
 * Abstract entity implementation.
 * 
 * @author Pierre-Alexandre (contact@b3dgs.com)
 */
public abstract class Entity
        extends EntityStrategy
        implements SkilledServices<Skill>, Race
{
    /** Map reference. */
    protected final Map map;
    /** Factory skill. */
    private final FactorySkill factorySkill;
    /** Entity life. */
    private final Alterable life;
    /** Entity name. */
    private final String name;
    /** Entity icon number. */
    private final Sprite icon;
    /** Skilled model. */
    private final SkilledModel<Skill> skilled;
    /** Dead flag. */
    private boolean dead;
    /** Player owner (null if none). */
    private Player owner;
    /** Creation progress. */
    private int progress;

    /**
     * Constructor.
     * 
     * @param setup The setup reference.
     */
    protected Entity(SetupEntity setup)
    {
        super(setup, setup.map);
        map = setup.map;
        factorySkill = setup.factorySkill;
        skilled = new SkilledModel<>();
        final Configurable configurable = setup.getConfigurable();
        life = new Alterable(configurable.getInteger("life", "attributes"));
        setFov(configurable.getInteger("fov", "attributes"));
        name = configurable.getString("name");
        final SurfaceData surfaceData = configurable.getSurface();
        icon = Drawable.loadSprite(Core.MEDIA.create(AppWarcraft.ENTITIES_DIR, setup.race.getPath(),
                surfaceData.getIcon()));
        icon.load(false);
        dead = false;
        owner = null;
        life.fill();
        progress = 100;
    }

    /**
     * Add a skill.
     * 
     * @param panel The skill panel.
     * @param type The skill type.
     * @param priority The position number.
     */
    public void addSkill(int panel, Class<? extends Skill> type, int priority)
    {
        final Skill skill = factorySkill.create(type);
        skill.setOwner(this);
        skill.setPriority(priority);
        skill.prepare();
        addSkill(skill, panel);
    }

    /**
     * Kill the entity.
     */
    public void kill()
    {
        decreaseLife(life.getCurrent());
    }

    /**
     * Get the current life.
     * 
     * @return The current life.
     */
    public int getLife()
    {
        return life.getCurrent();
    }

    /**
     * Get the life percent.
     * 
     * @return The life percent.
     */
    public int getLifePercent()
    {
        return life.getPercent();
    }

    /**
     * Decrease life value.
     * 
     * @param damages The damages.
     */
    public void decreaseLife(int damages)
    {
        decreaseLife(damages, null);
    }

    /**
     * Decrease life value.
     * 
     * @param damages The damages.
     * @param attacker The attacker reference.
     */
    public void decreaseLife(int damages, Attacker attacker)
    {
        life.decrease(damages);
        if (life.isEmpty())
        {
            dead = true;
            setSelectable(false);
            setActive(false);
            setAlive(false);
            stop();
            removeRef();
            final Player player = getPlayer();
            if (player != null)
            {
                player.changePopulation(-1);
            }
        }
    }

    /**
     * Get the entity name.
     * 
     * @return The entity name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the entity icon number.
     * 
     * @return The icon number.
     */
    public Sprite getIcon()
    {
        return icon;
    }

    /**
     * Get the owner reference.
     * 
     * @return The owner reference.
     */
    public Player getPlayer()
    {
        return owner;
    }

    /**
     * Set the owner reference.
     * 
     * @param player The player owner reference.
     */
    public void setPlayer(Player player)
    {
        owner = player;
        setPlayerId(player.id);
    }

    /**
     * Get the dead status.
     * 
     * @return <code>true</code> if dead, <code>false</code> else.
     */
    public boolean isDead()
    {
        return dead;
    }

    /**
     * Get the construction progress in percent.
     * 
     * @return The construction progress in percent.
     */
    public int getProgressPercent()
    {
        return progress;
    }

    /**
     * Set the current progress in percent.
     * 
     * @param progress The progress value in percent.
     */
    void setProgressPercent(int progress)
    {
        this.progress = progress;
    }

    /*
     * SkilledServices
     */

    @Override
    public void addSkill(Skill skill, int panel)
    {
        skilled.addSkill(skill, panel);
    }

    @Override
    public <S extends Skill> S getSkill(int panel, Class<S> id)
    {
        return skilled.getSkill(panel, id);
    }

    @Override
    public void removeSkill(int panel, Class<? extends Skill> id)
    {
        skilled.removeSkill(panel, id);
    }

    @Override
    public Collection<Skill> getSkills(int panel)
    {
        return skilled.getSkills(panel);
    }

    @Override
    public Collection<Skill> getSkills()
    {
        return skilled.getSkills();
    }

    @Override
    public void setSkillPanel(int currentSkillPanel)
    {
        skilled.setSkillPanel(currentSkillPanel);
    }

    @Override
    public void setSkillPanelNext(int nextSkillPanel)
    {
        skilled.setSkillPanelNext(nextSkillPanel);
    }

    @Override
    public int getSkillPanel()
    {
        return skilled.getSkillPanel();
    }
}
