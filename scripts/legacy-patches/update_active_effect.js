const fs = require('fs');

const aeFile = '/mnt/hdd/Code/db-arena/db-arena-java/src/main/java/com/pecodigos/dbarena/ingame/battle/models/ActiveEffect.java';
let aeCode = fs.readFileSync(aeFile, 'utf8');
aeCode = aeCode.replace('private boolean invisible;', 'private boolean invisible;\n    private String casterUsername;');
fs.writeFileSync(aeFile, aeCode);

const plFile = '/mnt/hdd/Code/db-arena/db-arena-java/src/main/java/com/pecodigos/dbarena/ingame/battle/models/Player.java';
let plCode = fs.readFileSync(plFile, 'utf8');
plCode = plCode.replace('Boolean.TRUE.equals(ability.getIsInvisible())', 'Boolean.TRUE.equals(ability.getIsInvisible()),\n                    this.getUserProfile().getUsername()');
fs.writeFileSync(plFile, plCode);

const testFile = '/mnt/hdd/Code/db-arena/db-arena-java/src/test/java/com/pecodigos/dbarena/ingame/battle/models/PlayerBattleLogicTests.java';
let testCode = fs.readFileSync(testFile, 'utf8');
testCode = testCode.replace('new ActiveEffect("dot", "Some dot desc", "dot.png", 2, true)', 'new ActiveEffect("dot", "Some dot desc", "dot.png", 2, true, false, "Unknown")');
fs.writeFileSync(testFile, testCode);
