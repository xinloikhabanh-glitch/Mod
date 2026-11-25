# AI Builder - mod Minecraft 1.16.5 (Forge 36.2.39)

Go lenh `/aibuild <mo ta>` trong game, AI se thiet ke cong trinh va mod tu dong xay truoc mat ban.

    /aibuild lau dai trung co khung lo voi 4 thap, ho nuoc bao quanh, cau treo
    /aibuild thanh pho tren khong kieu fantasy co cau noi giua cac dao
    /aibuild ngoi den Nhat Ban 5 tang ben ho sen, co cong torii

## Lenh

| Lenh | Tac dung |
|---|---|
| `/aibuild <mo ta>` | AI thiet ke roi xay. Mat truoc cong trinh quay ve phia ban. |
| `/aibuild undo` | Go cong trinh gan nhat, tra lai khoi cu (nho 3 lan gan nhat). |
| `/aibuild cancel` | Dung cong trinh dang xay do. |
| `/aibuild demo` | Xay nha mau, KHONG goi AI (de thu mod co chay khong). |
| `/aibuild file <ten>` | Xay lai ban thiet ke da luu trong `config/aibuilder/blueprints/<ten>.json` (khong ton them tien API). |

Moi cong trinh AI tao ra duoc luu tu dong o `config/aibuilder/blueprints/`.

## 1. Build ra file .jar (chon 1 cach)

Luu y: du an nay chua duoc bien dich thu voi Minecraft that (noi tao ra no khong tai duoc Forge).
Phan logic sinh khoi da duoc test, nhung neu build bao loi o dong nao thi gui lai cho minh sua.

**Cach A - GitHub Actions (khong can cai gi):**
1. Tao repo GitHub, upload toan bo thu muc nay (gom `.github`).
2. Tab *Actions* -> *Build mod* -> *Run workflow*.
3. Xong, tai `aibuilder-jar` o muc *Artifacts*, giai nen ra file `aibuilder-1.16.5-1.0.0.jar`.

**Cach B - Tren may:**
1. Cai JDK 8 va Gradle 7.6.x.
2. Trong thu muc nay chay: `gradle build`
3. File jar o `build/libs/`.

**Cach C - Dung Forge MDK:** tai MDK 1.16.5-36.2.39 tu files.minecraftforge.net, chep thu muc `src` cua du an nay de len `src` cua MDK, roi chay `gradlew build`.

## 2. Cai vao modpack

1. Cai Forge 1.16.5 (36.2.39 tro len) cho profile/instance.
2. Bo file jar vao thu muc `mods`.
3. Chay game 1 lan de mod tao file `config/aibuilder-common.toml`.
4. Mo file do, dien `apiKey`, `apiUrl`, `model` (xem muc 3), luu lai.
5. Vao the gioi, go `/aibuild demo` de thu, roi `/aibuild <mo ta>`.

Dong goi modpack (CurseForge / Prism / MultiMC): xuat instance nhu binh thuong. **DUNG de apiKey cua ban trong file config khi chia se modpack** - hay xoa key truoc khi xuat, de moi nguoi tu nhap key cua ho.
Neu chay server rieng, cai mod o ca server va client, dat `permissionLevel = 2` de chi OP dung duoc (tranh ton tien API).

## 3. Cau hinh AI (`config/aibuilder-common.toml`)

Gemini (mac dinh, lay key mien phi o https://aistudio.google.com/apikey):

    apiType = "openai"
    apiUrl  = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
    apiKey  = "AIza..."
    model   = "gemini-2.5-pro"

`gemini-2.5-pro` cho cong trinh dep nhat, `gemini-2.5-flash` nhanh va re hon. Neu Google da ra ban moi hon thi dung ten model moi trong AI Studio. Giu `maxTokens` >= 32000 vi Gemini tinh ca token "suy nghi". Goi mien phi co gioi han so luot/phut, neu bao loi 429 thi doi mot luc roi thu lai.

OpenAI hoac may chu tuong thich (OpenRouter, Groq, DeepSeek, Ollama...):

    apiType = "openai"
    apiUrl  = "https://api.openai.com/v1/chat/completions"
    apiKey  = "sk-..."
    model   = "gpt-4o"

Claude (Anthropic):

    apiType = "anthropic"
    apiUrl  = "https://api.anthropic.com/v1/messages"
    apiKey  = "sk-ant-..."
    model   = "<ten model Claude hien tai>"

Mot so thong so khac: `maxTokens` (cong trinh lon can nhieu), `blocksPerTick` (toc do xay, giam neu lag), `maxBlocks`, `maxRadius`, `distance`.
Muon doi cach AI thiet ke: tao file `config/aibuilder/system_prompt.txt` (se dung thay prompt mac dinh).

## Meo viet prompt

- Noi ro phong cach, vat lieu, kich thuoc ("rong khoang 80 khoi", "cao 60 khoi").
- Model manh hon = cong trinh dep va dung hon. Model nho de ra JSON loi.
- Dung o noi bang phang, thoang va trong tam nhin (view distance) de chunk da tai.

## Cach hoat dong

1. Lenh gui mo ta + system prompt (`aibuilder_prompt.txt`) toi AI qua HTTPS (thread rieng, khong lam dung game).
2. AI tra ve blueprint JSON gom bang mau (palette) va cac lenh `fill / hollow / walls / line / sphere / cylinder / pyramid / cone / set`.
3. Mod chuyen thanh danh sach khoi, xoay theo huong ban dung, roi dat dan dan moi tick (tu duoi len), sau do cap nhat lai hang rao/kinh/tuong/cau thang cho noi voi nhau.
4. Block khong ton tai bi thay bang stone_bricks; command block, structure block, bedrock, barrier bi chan.

## Han che

- Hoan tac khong khoi phuc noi dung ruong/bien (tile entity) bi ghi de.
- Game Minecraft Launcher goc dung Java 8 ban cu co the loi chung chi HTTPS; neu bao loi SSL, dung Java 8 moi (Temurin 8u3xx) cho instance.
- Chat luong cong trinh phu thuoc model AI.
