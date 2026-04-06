const fs = require('fs');
const plFile = '/mnt/hdd/Code/db-arena/db-arena-java/src/main/java/com/pecodigos/dbarena/ingame/battle/models/Player.java';
let plCode = fs.readFileSync(plFile, 'utf8');
plCode = plCode.replace('this.getUserProfile().getUsername()', 'this.getUserProfile().username()');
fs.writeFileSync(plFile, plCode);
