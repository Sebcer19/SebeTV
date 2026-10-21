package fr.sebcer.sebetv;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Local-only TV launcher. No accounts, network calls or privileged settings changes. */
public class MainActivity extends Activity {
    private android.content.SharedPreferences prefs;
    private final ArrayList<ResolveInfo> apps = new ArrayList<>();
    private LinearLayout content;
    private boolean allApps = false;
    private String restoreFocus = "";
    private int theme;
    private final int[] accents = {0xffe8b778, 0xffa9b8ff, 0xff84dbc9};
    private int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("sebetv", MODE_PRIVATE);
        if (state != null) { allApps = state.getBoolean("all"); restoreFocus = state.getString("focus", ""); }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("all", allApps); state.putString("focus", restoreFocus);
        super.onSaveInstanceState(state);
    }
    @Override protected void onResume() { super.onResume(); reloadApps(); render(); }
    private void reloadApps() {
        apps.clear();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        Set<String> seen = new HashSet<>();
        for (ResolveInfo info : getPackageManager().queryIntentActivities(query, 0)) {
            String pkg = info.activityInfo.packageName;
            if (!pkg.equals(getPackageName()) && seen.add(pkg)) apps.add(info);
        }
        apps.sort((a,b) -> a.loadLabel(getPackageManager()).toString().compareToIgnoreCase(b.loadLabel(getPackageManager()).toString()));
    }
    private Set<String> favorites() { return new HashSet<>(prefs.getStringSet("favorites", Collections.emptySet())); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private GradientDrawable surface(boolean focused) {
        GradientDrawable bg = new GradientDrawable(); bg.setColor(focused ? 0xff303644 : 0xe619202c);
        bg.setCornerRadius(dp(12)); bg.setStroke(dp(focused ? 3 : 1), focused ? accents[theme] : 0x304f586d); return bg;
    }
    private void focusable(View v, String tag) {
        v.setTag(tag); v.setFocusable(true); v.setClickable(true); v.setBackground(surface(false));
        v.setOnFocusChangeListener((view, focus) -> {
            view.setBackground(surface(focus));
            view.animate().scaleX(focus ? 1.035f : 1f).scaleY(focus ? 1.035f : 1f).setDuration(130).start();
            if (focus) restoreFocus = tag;
        });
    }
    private TextView button(String label, String tag, Runnable action) {
        TextView b = text(label, 14, Color.WHITE); b.setGravity(Gravity.CENTER); b.setPadding(dp(18), dp(10), dp(18), dp(10));
        focusable(b, tag); b.setOnClickListener(v -> action.run()); return b;
    }
    private TextView tab(String label, String tag, boolean selected, Runnable action) {
        TextView tab = button(label, tag, action);
        tab.setSelected(selected);
        tab.setTextColor(selected ? accents[theme] : Color.WHITE);
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tab.setContentDescription(label + (selected ? ", sélectionné" : ""));
        return tab;
    }
    private boolean isEntertainment(ResolveInfo app) {
        String name = (app.activityInfo.packageName + " " + app.loadLabel(getPackageManager())).toLowerCase(Locale.ROOT);
        String[] brands = {"netflix", "youtube", "disney", "amazonvideo", "prime video", "canal", "crunchyroll", "apple", "spotify", "plex", "jellyfin", "kodi", "molotov", "france.tv", "francetv", "arte", "twitch", "max.hbo", "hbo", "tivimate", "mytf1", "tf1", "m6", "pluto", "paramount", "vlc"};
        for (String brand : brands) if (name.contains(brand)) return true;
        return false;
    }
    private void render() {
        theme = prefs.getInt("theme", 0);
        FrameLayout frame = new FrameLayout(this);
        frame.addView(new Landscape(this), new FrameLayout.LayoutParams(-1,-1));
        LinearLayout shell = new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(38),dp(24),dp(38),dp(14)); frame.addView(shell);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("SÉBÉ TV", 19, accents[theme]); brand.setTypeface(null, Typeface.BOLD);
        header.addView(brand, new LinearLayout.LayoutParams(0,-2,1));
        addNav(header, tab("Accueil", "home", !allApps, () -> { allApps = false; restoreFocus="home"; render(); }));
        addNav(header, tab("Applications", "apps", allApps, () -> { allApps = true; restoreFocus="apps"; render(); }));
        addNav(header, button("Ambiance", "theme", this::chooseTheme));
        addNav(header, button("Réglages", "settings", () -> safeStart(new Intent(Settings.ACTION_SETTINGS))));
        TextClock clock = new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("HH:mm"); clock.setTextSize(18); clock.setTextColor(Color.WHITE);
        clock.setPadding(dp(12),0,0,0); header.addView(clock); shell.addView(header);
        View rule = new View(this); rule.setBackgroundColor(0x224e5a70);
        LinearLayout.LayoutParams ruleParams = new LinearLayout.LayoutParams(-1,dp(1)); ruleParams.topMargin=dp(17); shell.addView(rule,ruleParams);
        ScrollView scroll = new ScrollView(this); scroll.setClipToPadding(false); scroll.setVerticalScrollBarEnabled(false);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(5),dp(20),dp(5),dp(12));
        scroll.addView(content); shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Set<String> fav = favorites();
        if (allApps) {
            int columns = Math.max(2, Math.min(5, (getResources().getConfiguration().screenWidthDp - 86) / 150));
            for (int start=0;start<apps.size();start+=columns) {
                LinearLayout row = new LinearLayout(this); row.setPadding(0,dp(7),0,dp(7)); row.setClipChildren(false);
                for (int col=0;col<columns;col++) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,dp(111),1); lp.setMargins(dp(5),0,dp(5),0);
                    row.addView(start+col<apps.size()?card(apps.get(start+col),fav):new View(this),lp);
                }
                content.addView(row);
            }
        } else {
            ArrayList<ResolveInfo> selected = new ArrayList<>(), streaming = new ArrayList<>();
            for (ResolveInfo app : apps) {
                if (fav.contains(app.activityInfo.packageName)) selected.add(app);
                else if (isEntertainment(app)) streaming.add(app);
            }
            if (!selected.isEmpty()) appShelf("Vos favoris", "Votre sélection personnelle",selected,fav);
            if (!streaming.isEmpty()) appShelf("Films, séries & musique", "Explorez vos applications",streaming,fav);
            if (selected.isEmpty() && streaming.isEmpty()) appShelf("Vos applications", "Choisissez vos premiers favoris",apps,fav);

        }
        if (apps.isEmpty()) content.addView(text("Aucune application TV détectée. Installez-en depuis le Play Store.",16,Color.WHITE));
        setContentView(frame);
        View target = shell.findViewWithTag(restoreFocus);
        if (target == null && !allApps) target = firstCard(content);
        if (target == null) target = shell.findViewWithTag("apps");
        if (target != null) target.requestFocus();
    }
    private View firstCard(ViewGroup parent) {
        for(int i=0;i<parent.getChildCount();i++) {
            View v=parent.getChildAt(i);
            if(v.isFocusable() && v.getTag()!=null) return v;
            if(v instanceof ViewGroup) { View found=firstCard((ViewGroup)v); if(found!=null)return found; }
        }
        return null;
    }
    private void appShelf(String name, String description, List<ResolveInfo> items, Set<String> fav) {
        if(items.isEmpty()) return;
        LinearLayout heading=new LinearLayout(this); heading.setGravity(Gravity.CENTER_VERTICAL); heading.setPadding(0,dp(4),0,dp(4));
        TextView title=text(name,17,Color.WHITE); title.setTypeface(null,Typeface.BOLD);
        heading.addView(title,new LinearLayout.LayoutParams(0,-2,1)); content.addView(heading);
        HorizontalScrollView shelf=new HorizontalScrollView(this); shelf.setHorizontalScrollBarEnabled(false); shelf.setClipToPadding(false);
        LinearLayout row=new LinearLayout(this); row.setPadding(dp(5),dp(7),dp(5),dp(9)); row.setClipChildren(false);
        int width=Math.max(140,(getResources().getConfiguration().screenWidthDp-110)/5);
        for(ResolveInfo app:items) { LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(width),dp(111)); lp.rightMargin=dp(12); row.addView(card(app,fav),lp); }
        shelf.addView(row); content.addView(shelf);
    }
    private void addNav(LinearLayout row, View v) { LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2,-2); lp.rightMargin=dp(12); row.addView(v,lp); }
    private View card(ResolveInfo app, Set<String> fav) {
        String pkg = app.activityInfo.packageName;
        LinearLayout tile = new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(9),dp(8),dp(9),dp(8));
        ImageView icon = new ImageView(this); android.graphics.drawable.Drawable banner = app.activityInfo.loadBanner(getPackageManager());
        if (banner == null) banner = app.activityInfo.applicationInfo.loadBanner(getPackageManager());
        icon.setImageDrawable(banner != null ? banner : app.loadIcon(getPackageManager()));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER); icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(icon,new LinearLayout.LayoutParams(banner != null ? -1 : dp(48),dp(61)));
        String name = app.loadLabel(getPackageManager()).toString();
        TextView label = text((fav.contains(pkg) ? "★ " : "") + name, 13, Color.WHITE); label.setGravity(Gravity.CENTER); label.setMaxLines(1); label.setEllipsize(android.text.TextUtils.TruncateAt.END); label.setPadding(0,dp(8),0,0); tile.addView(label);
        tile.setContentDescription(name + (fav.contains(pkg) ? ", favori" : "")); focusable(tile,pkg);
        tile.setOnClickListener(v -> safeStart(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER).setComponent(new ComponentName(pkg,app.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        tile.setOnLongClickListener(v -> {
            Set<String> current = favorites(); boolean exists = current.contains(pkg);
            new AlertDialog.Builder(this).setTitle(name).setItems(new String[]{exists ? "Retirer des favoris" : "Ajouter aux favoris"}, (d,which) -> {
                if (exists) current.remove(pkg); else current.add(pkg);
                prefs.edit().putStringSet("favorites",current).apply(); render();
            }).setNegativeButton("Annuler",null).show(); return true;
        });
        return tile;
    }
    private void chooseTheme() {
        new AlertDialog.Builder(this).setTitle("Votre ambiance").setSingleChoiceItems(new String[]{"Cinéma · ambre", "Minuit · bleu", "Nature · sauge"},theme,(dialog,which) -> {
            prefs.edit().putInt("theme",which).apply(); dialog.dismiss(); restoreFocus="theme"; render();
        }).setNegativeButton("Fermer",null).show();
    }
    private void safeStart(Intent intent) {
        try { startActivity(intent); } catch (ActivityNotFoundException | SecurityException e) { Toast.makeText(this,"Cette application n’est pas accessible sur cette télé.",Toast.LENGTH_LONG).show(); }
    }
    @Override public void onBackPressed() {
        if (allApps) { allApps=false; restoreFocus="home"; render(); }
        else new AlertDialog.Builder(this).setTitle("Quitter Sébé TV ?").setMessage("Revenir à l’écran précédent.").setPositiveButton("Quitter",(d,w)->finish()).setNegativeButton("Rester",null).show();
    }
    private class Landscape extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path[] ridges={new Path(),new Path(),new Path()};
        private final int[] ridgeColors={0x244a526b,0x333d4963,0x88101825};
        private Shader sky,glow,veil;
        Landscape(Context c) { super(c); setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO); }
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
            int[] tones={0xff604b35,0xff354c84,0xff2c675e};
            sky=new LinearGradient(0,0,w,h,0xff090e19,0xff151e30,Shader.TileMode.CLAMP);
            glow=new RadialGradient(w*.88f,h*.24f,w*.6f,new int[]{tones[theme],0x00151e30},null,Shader.TileMode.CLAMP);
            veil=new LinearGradient(0,0,0,h,new int[]{0x11060b15,0x44060b15,0xf2080d17},null,Shader.TileMode.CLAMP);
            for(int i=0;i<3;i++) {
                Path p=ridges[i]; p.reset(); p.moveTo(0,h); p.lineTo(0,h*(.79f+i*.04f));
                p.cubicTo(w*.2f,h*(.55f+i*.1f),w*.35f,h*(.98f-i*.05f),w*.57f,h*(.64f+i*.08f));
                p.cubicTo(w*.75f,h*(.41f+i*.12f),w*.82f,h*(.65f+i*.09f),w,h*(.44f+i*.15f)); p.lineTo(w,h); p.close();
            }
        }
        @Override protected void onDraw(Canvas canvas) {
            float w=getWidth(),h=getHeight();
            paint.setShader(sky); canvas.drawRect(0,0,w,h,paint);
            paint.setShader(glow); canvas.drawRect(0,0,w,h,paint); paint.setShader(null);
            for(int i=0;i<3;i++) { paint.setColor(ridgeColors[i]); canvas.drawPath(ridges[i],paint); }
            paint.setShader(veil); canvas.drawRect(0,0,w,h,paint); paint.setShader(null);
        }
    }
}
