const fs = require('fs');
const file = '/mnt/hdd/Code/db-arena/db-arena-angular/src/app/in-game/battle/battle.component.scss';
let scss = fs.readFileSync(file, 'utf8');

const regexTooltip = /\.custom-effect-tooltip\s*\{[\s\S]*?\}/;

const newTooltipStyles = \`.custom-effect-tooltip {
  display: none;
  position: absolute;
  top: 130%;
  left: 50%;
  transform: translateX(-50%);
  width: max-content;
  max-width: 22rem;
  background-color: rgb(253, 250, 216);
  border: 2px solid #b31c19;
  color: black;
  padding: 0.5rem 0.75rem;
  font-family: 'Libre Franklin', monospace;
  font-size: 0.65rem;
  z-index: 1000;
  text-align: left;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
  pointer-events: none;
}

.tooltip-title {
  color: #b31c19;
  font-weight: 800;
  font-size: 0.75rem;
  margin-bottom: 0.3rem;
  text-transform: uppercase;
}

.tooltip-desc {
  color: black;
  font-weight: 700;
  font-size: 0.7rem;
  margin-bottom: 0.6rem;
  text-transform: uppercase;
  line-height: 1.2;
}

.tooltip-desc::before {
  content: '- ';
}

.tooltip-turns {
  color: #b31c19;
  font-weight: 800;
  font-size: 0.75rem;
  text-transform: uppercase;
}\`;

scss = scss.replace(regexTooltip, newTooltipStyles);

fs.writeFileSync(file, scss);
