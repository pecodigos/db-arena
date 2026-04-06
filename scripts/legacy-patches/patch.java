            if (clone.getPlayerOne() != null && clone.getPlayerOne().getTeam() != null) {
                for (com.pecodigos.dbarena.ingame.battle.models.Fighter f : clone.getPlayerOne().getTeam()) {
                    if (f != null && f.getActiveEffects() != null) {
                        f.getActiveEffects().removeIf(effect -> effect.isInvisible() && !viewerUsername.equals(effect.getCasterUsername()));
                    }
                    if (f != null && f.getSkills() != null && !viewerUsername.equals(clone.getPlayerOne().getUserProfile().username())) {
                        for (com.pecodigos.dbarena.ingame.battle.models.Skill skill : f.getSkills()) {
                            if (skill.getAbility() != null && Boolean.TRUE.equals(skill.getAbility().getIsInvisible())) {
                                skill.setCurrentCooldown(0);
                            }
                        }
                    }
                }
            }

            if (clone.getPlayerTwo() != null && clone.getPlayerTwo().getTeam() != null) {
                for (com.pecodigos.dbarena.ingame.battle.models.Fighter f : clone.getPlayerTwo().getTeam()) {
                    if (f != null && f.getActiveEffects() != null) {
                        f.getActiveEffects().removeIf(effect -> effect.isInvisible() && !viewerUsername.equals(effect.getCasterUsername()));
                    }
                    if (f != null && f.getSkills() != null && !viewerUsername.equals(clone.getPlayerTwo().getUserProfile().username())) {
                        for (com.pecodigos.dbarena.ingame.battle.models.Skill skill : f.getSkills()) {
                            if (skill.getAbility() != null && Boolean.TRUE.equals(skill.getAbility().getIsInvisible())) {
                                skill.setCurrentCooldown(0);
                            }
                        }
                    }
                }
            }

            return clone;
        } catch (Exception e) {
