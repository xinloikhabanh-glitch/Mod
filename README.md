# Titan Bosses - mod Minecraft 1.16.5 (Forge 36.2.39)

Them **5 boss** manh hon boss goc >= 5 lan va **bo do Titanite** manh/ben gap 4 lan netherite.

## 5 boss (chi trieu hoi duoc bang An/Sigil, khong tu spawn)

Boss goc 1.16.5 de so sanh: Ender Dragon 200 HP, Wither 300 HP, Elder Guardian 80 HP, Ravager 100 HP.

| Boss | May | Sat thuong/don | Chieu thuc | Sigil can them |
|---|---|---|---|---|
| Crypt Titan | 1500 | 50 | Weakness quanh minh, goi zombie | rotten_flesh |
| Blaze Overlord | 1600 | 40 | Cau lua no lon, ban lien tuc | blaze_rod |
| Venom Matriarch | 1800 | 55 | Doc + lam cham, de nhen hang | spider_eye |
| Wither Lord | 2000 | 60 | Hieu ung Wither, goi Wither Skeleton | wither_skeleton_skull |
| Storm Ravager (boss cuoi) | 2500 | 75 | Set danh (+30 sat thuong) | dragon_breath |

Tat ca co giap 20-28, khang day 90%, mien lua, thanh mau boss, khong bien mat khi di xa.
Moi boss roi **Titanite Shard** (4-16 manh) va **Nether Star**.

## Bo do Titanite

| | Netherite | Titanite |
|---|---|---|
| Kiem (sat thuong/don) | 8 | **32** |
| Riu | 10 | **40** |
| Toc do dao | 9 | **36** |
| Do ben cong cu | 2031 | **8124** |
| Do ben giap (he so) | 37 | **148** |
| Giap moi manh | 3/6/8/3 | **12/24/32/12** (Minecraft gioi han tong giap 30) |
| Toughness moi manh | 3 | **12** |
| Chong day moi manh | 0.1 | **0.4** |

Minecraft gioi han giap va giam sat thuong toi da 80%. De giap Titanite van manh hon thuc su, **moi manh Titanite giam them 12% sat thuong** (du bo = giam them 48%).

## Cong thuc

- **Titanite Ingot** = 4 Titanite Shard + 1 Netherite Ingot (xep tu do trong o che tao).
- **Do Titanite** = nang cap tai **Smithing Table**: do netherite tuong ung + Titanite Ingot. Phu phep giu nguyen.
- **Sigil trieu hoi** (xep tu do): Nether Star + Netherite Ingot + Totem of Undying + vat pham rieng cua tung boss (xem bang tren).

Chuot phai Sigil xuong mat dat de trieu hoi boss. Che do Creative khong ton Sigil.
Lenh nhanh de thu: `/give @s titanbosses:storm_ravager_sigil` hoac `/summon titanbosses:crypt_titan`.

## Luu y

- Boss cuc manh: nen co giap netherite + Protection IV, thuoc hoi, ngoc tien xa. Dung o noi rong, bang phang.
- Blaze Overlord ban cau lua no pha khoi neu `mobGriefing` bat. Muon giu cong trinh cua ban: `/gamerule mobGriefing false`.
- Do Titanite hien dung texture netherite (khac ten, khac chi so). Muon texture rieng: thay file trong `assets/titanbosses/models/item/` hoac them png vao resource pack.
- Boss dung model cua mob goc phong to len.

## Build

Giong mod AI Builder: upload toan bo thu muc len GitHub -> Actions -> Build mod -> tai artifact -> giai nen ra file `.jar` -> bo vao `mods`.
Neu dung chung modpack voi AI Builder, cu bo ca hai file jar vao `mods`.
