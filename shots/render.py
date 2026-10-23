#!/usr/bin/env python3
"""Render BorrowTrack app screenshots with PIL (2x scale)."""
from PIL import Image, ImageDraw, ImageFont
import os

S = 2
W, H = 360 * S, 740 * S
BG = (14, 13, 11)
SURF = (23, 20, 15)
BORD = (42, 36, 26)
AMBER = (245, 158, 11)
AMBER_D = (28, 16, 5)
WHITE = (243, 239, 232)
GREY = (168, 159, 141)
GREY2 = (110, 102, 86)
RED = (248, 113, 113)
RED_BG = (60, 25, 25)
AMB_BG = (62, 45, 15)
GRN = (74, 222, 128)
GRN_BG = (22, 60, 38)

FD = "/usr/share/fonts/truetype/noto"
HOME_F = os.path.expanduser("~/.fonts")

def font(name, size, bold=False):
    # ~/.fonts/NotoSansMalayalam.ttf (504KB) has full Latin+Malayalam coverage;
    # the /usr/share subset lacks Latin glyphs.
    p = os.path.expanduser("~/.fonts/NotoSansMalayalam.ttf")
    f = ImageFont.truetype(p, size * S, layout_engine=ImageFont.Layout.RAQM)
    if bold:
        f.stroke_width = max(1, size * S // 28)
    return f

def efont(size):
    return ImageFont.truetype(f"{FD}/NotoColorEmoji.ttf", size * S)

def rr(d, box, r, fill, outline=None, width=1):
    b = [v * S for v in box]
    d.rounded_rectangle(b, radius=r * S, fill=fill, outline=outline, width=width * S)

def txt(d, xy, s, f, fill, anchor="la"):
    d.text((xy[0] * S, xy[1] * S), s, font=f, fill=fill, anchor=anchor)

def emoji(d, xy, s, size, anchor="la"):
    pass  # replaced by vector icons below

def icon_home(d, cx, cy, col):
    s = S
    # roof
    d.polygon([(cx - 11) * s, cy * s, (cx + 11) * s, cy * s, cx * s, (cy - 9) * s], fill=col)
    # body
    d.rectangle([(cx - 8) * s, cy * s, (cx + 8) * s, (cy + 10) * s], fill=col)

def icon_bell(d, cx, cy, col):
    s = S
    # dome
    d.pieslice([(cx - 8) * s, (cy - 8) * s, (cx + 8) * s, (cy + 8) * s], 180, 360, fill=col)
    d.rectangle([(cx - 8) * s, cy * s, (cx + 8) * s, (cy + 4) * s], fill=col)
    # clapper
    d.ellipse([(cx - 3) * s, (cy + 5) * s, (cx + 3) * s, (cy + 11) * s], fill=col)
    # knob
    d.ellipse([(cx - 2) * s, (cy - 12) * s, (cx + 2) * s, (cy - 8) * s], fill=col)

def icon_list(d, cx, cy, col):
    s = S
    d.rounded_rectangle([(cx - 9) * s, (cy - 10) * s, (cx + 9) * s, (cy + 10) * s],
                        radius=3 * s, outline=col, width=2 * s)
    for i, yy in enumerate([cy - 4, cy + 1, cy + 6]):
        d.line([(cx - 5) * s, yy * s, (cx + 5) * s, yy * s], fill=col, width=2 * s)

def base():
    im = Image.new("RGB", (W, H), BG)
    return im, ImageDraw.Draw(im)

def appbar(d, title_left="Borrow", title_right="Track", back=False, title=""):
    if back:
        txt(d, (16, 52), "‹", font("b", 30, True), WHITE)
        txt(d, (44, 58), title, font("b", 17, True), WHITE)
    else:
        txt(d, (16, 56), title_left, font("b", 20, True), WHITE)
        w = d.textlength(title_left, font=font("b", 20, True))
        d.text((16 * S + w, 56 * S), title_right, font=font("b", 20, True), fill=AMBER, anchor="la")
        # FAB
        cx, cy, r = 360 - 16 - 20, 56 + 14, 20
        d.ellipse([(cx - r) * S, (cy - r) * S, (cx + r) * S, (cy + r) * S], fill=AMBER)
        txt(d, (cx, cy - 1), "+", font("b", 22, True), AMBER_D, anchor="mm")

def navbar(d, active=0):
    y = 740 - 64
    d.rectangle([0, y * S, W, H], fill=(16, 14, 11))
    d.line([0, y * S, W, y * S], fill=BORD, width=S)
    items = [(icon_home, "Home"), (icon_bell, "Reminders"), (icon_list, "History")]
    for i, (icfn, lb) in enumerate(items):
        cx = 360 * (i + 0.5) / 3
        on = i == active
        icfn(d, cx, y + 20, AMBER if on else GREY2)
        txt(d, (cx, y + 40), lb, font("b" if on else "", 10, on), AMBER if on else GREY2, anchor="ma")

def badge(d, x, y, text, fg, bg):
    f = font("b", 10, True)
    w = d.textlength(text, font=f) / S + 20
    rr(d, [x, y, x + w, y + 20], 10, bg)
    txt(d, (x + w / 2, y + 10), text, f, fg, anchor="mm")
    return w

def card(d, y, item, btext, bfg, bbg, line1, line2):
    rr(d, [14, y, 346, y + 86], 14, SURF, BORD)
    txt(d, (28, y + 12), item, font("b", 14, True), WHITE)
    f10 = font("b", 10, True)
    bw = d.textlength(btext, font=f10) / S + 20
    bx = 346 - 14 - bw
    badge(d, bx, y + 12, btext, bfg, bbg)
    txt(d, (28, y + 38), line1, font("", 12), GREY)
    t2 = txt(d, (28, y + 58), line2, font("", 11.5), GREY)
    return y + 96

def flabel(d, y, s):
    txt(d, (18, y), s, font("b", 11, True), GREY)
    return y + 20

def field(d, y, s, dim=False):
    rr(d, [18, y, 342, y + 44], 12, SURF, BORD)
    txt(d, (32, y + 22), s, font("", 13.5), GREY2 if dim else WHITE, anchor="lm")
    return y + 44

# ---------------- SCREEN 1: HOME ----------------
im, d = base()
appbar(d)
rr(d, [14, 108, 172, 172], 14, SURF, BORD)
txt(d, (27, 118), "3", font("b", 22, True), AMBER)
txt(d, (27, 146), "തിരികെ കിട്ടാനുള്ളത്", font("", 11), GREY)
rr(d, [182, 108, 346, 172], 14, SURF, BORD)
txt(d, (195, 118), "2", font("b", 22, True), AMBER)
txt(d, (195, 146), "തിരികെ കൊടുക്കാനുള്ളത്", font("", 11), GREY)
rr(d, [14, 182, 346, 222], 12, SURF, BORD)
rr(d, [17, 185, 178, 219], 9, AMBER)
txt(d, (97.5, 202), "കൊടുത്തത്", font("b", 13, True), AMBER_D, anchor="mm")
txt(d, (262, 202), "വാങ്ങിയത്", font("b", 13, True), GREY, anchor="mm")
y = 234
y = card(d, y, "₹500", "OVERDUE", RED, RED_BG, "Priya · 3 ദിവസം overdue", "Sep 28 ന് തിരികെ വേണ്ടതായിരുന്നു")
y = card(d, y, "Python book", "DUE SOON", AMBER, AMB_BG, "Arun · കൊടുത്തത് Sep 20", "Return by Oct 5")
y = card(d, y, "Charger", "OK", GRN, GRN_BG, "Rahul · കൊടുത്തത് Sep 25", "Return by Oct 10")
navbar(d, 0)
im.save(os.path.expanduser("~/workspace/borrowtrack/screenshots/home.png"))

# ---------------- SCREEN 2: ADD ----------------
im, d = base()
appbar(d, back=True, title="പുതിയ entry")
rr(d, [18, 108, 342, 150], 12, SURF, BORD)
rr(d, [21, 111, 178, 147], 9, AMBER)
txt(d, (99.5, 129), "ഞാൻ കൊടുത്തു", font("b", 13, True), AMBER_D, anchor="mm")
txt(d, (261, 129), "ഞാൻ വാങ്ങി", font("b", 13, True), GREY, anchor="mm")
y = 162
y = flabel(d, y, "ആര്"); y = field(d, y, "Priya") + 4
y = flabel(d, y, "എന്ത്"); y = field(d, y, "₹500") + 4
y = flabel(d, y, "തീയതി"); y = field(d, y, "25 Sep 2026", True) + 4
y = flabel(d, y, "തിരികെ തീയതി"); y = field(d, y, "5 Oct 2026", True) + 4
y = flabel(d, y, "കുറിപ്പ് (optional)"); y = field(d, y, "എഴുതൂ…", True) + 4
rr(d, [18, 740 - 64 - 62, 342, 740 - 64 - 14], 14, AMBER)
txt(d, (180, 740 - 64 - 38), "Save", font("b", 15, True), AMBER_D, anchor="mm")
navbar(d, 0)
im.save(os.path.expanduser("~/workspace/borrowtrack/screenshots/add.png"))

# ---------------- SCREEN 3: DETAIL ----------------
im, d = base()
appbar(d, back=True, title="Details")
badge(d, 18, 108, "OVERDUE · 3 days", RED, RED_BG)
txt(d, (18, 138), "₹500", font("b", 21, True), WHITE)
txt(d, (18, 168), "Priya-ക്ക് കൊടുത്തത്", font("", 13.5), GREY)
y = 196
y = flabel(d, y, "തീയതി"); y = field(d, y, "25 Sep 2026", True) + 4
y = flabel(d, y, "തിരികെ വേണ്ട തീയതി"); y = field(d, y, "28 Sep 2026", True) + 4
rr(d, [18, 740 - 64 - 118, 342, 740 - 64 - 70], 14, SURF, BORD)
txt(d, (180, 740 - 64 - 94), "Remind", font("b", 13, True), WHITE, anchor="mm")
rr(d, [18, 740 - 64 - 62, 342, 740 - 64 - 14], 14, AMBER)
txt(d, (180, 740 - 64 - 38), "തിരികെ കിട്ടി", font("b", 15, True), AMBER_D, anchor="mm")
navbar(d, 0)
im.save(os.path.expanduser("~/workspace/borrowtrack/screenshots/detail.png"))

print("screenshots done")
