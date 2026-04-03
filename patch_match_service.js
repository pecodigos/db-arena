const fs = require('fs');
const file = '/mnt/hdd/Code/db-arena/db-arena-java/src/main/java/com/pecodigos/dbarena/ingame/battle/services/MatchService.java';
let code = fs.readFileSync(file, 'utf8');

// replace 189
code = code.replace(
    'MatchInfoDTO matchInfoOne = new MatchInfoDTO(match, playerTwo.getUserProfile());',
    'MatchInfoDTO matchInfoOne = new MatchInfoDTO(getSanitizedMatch(match, playerOneUsername), playerTwo.getUserProfile());'
);

// replace 190
code = code.replace(
    'MatchInfoDTO matchInfoTwo = new MatchInfoDTO(match, playerOne.getUserProfile());',
    'MatchInfoDTO matchInfoTwo = new MatchInfoDTO(getSanitizedMatch(match, playerTwoUsername), playerOne.getUserProfile());'
);

// replace 315
code = code.replace(
    'return new MatchInfoDTO(match, opponentProfile);',
    'return new MatchInfoDTO(getSanitizedMatch(match, username), opponentProfile);'
);

// replace 620
code = code.replace(
    'new MatchInfoDTO(match, playerOneOpponentProfile)',
    'new MatchInfoDTO(getSanitizedMatch(match, playerOneUsername), playerOneOpponentProfile)'
);

// replace 626
code = code.replace(
    'new MatchInfoDTO(match, playerTwoOpponentProfile)',
    'new MatchInfoDTO(getSanitizedMatch(match, playerTwoUsername), playerTwoOpponentProfile)'
);

// inject method
const newMethod = `
    private Match getSanitizedMatch(Match match, String viewerUsername) {
        if (match == null) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Match clone = mapper.convertValue(match, Match.class);

            if (clone.getPlayerOne() != null && clone.getPlayerOne().getTeam() != null) {
                for (com.pecodigos.dbarena.ingame.battle.models.Fighter f : clone.getPlayerOne().getTeam()) {
                    if (f != null && f.getActiveEffects() != null) {
                        f.getActiveEffects().removeIf(effect -> effect.isInvisible() && !viewerUsername.equals(effect.getCasterUsername()));
                    }
                }
            }

            if (clone.getPlayerTwo() != null && clone.getPlayerTwo().getTeam() != null) {
                for (com.pecodigos.dbarena.ingame.battle.models.Fighter f : clone.getPlayerTwo().getTeam()) {
                    if (f != null && f.getActiveEffects() != null) {
                        f.getActiveEffects().removeIf(effect -> effect.isInvisible() && !viewerUsername.equals(effect.getCasterUsername()));
                    }
                }
            }

            return clone;
        } catch (Exception e) {
            log.error("Failed to sanitize match for user " + viewerUsername, e);
            return match;
        }
    }
}
`;

code = code.replace(/}\s*$/, newMethod);

fs.writeFileSync(file, code);
