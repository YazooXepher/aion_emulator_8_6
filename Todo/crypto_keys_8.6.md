# Aion 8.6 — Clés et formules cryptographiques

Compilé le 2026-07-22. Statut de confiance indiqué pour chaque bloc.

## 1. Protocole de jeu (AL-Game <-> client, opcodes S_/C_) — CONFIRMÉ

Validé de deux façons indépendantes : (a) déchiffre correctement des dizaines de
captures réseau réelles (officiel + notre serveur local) tout au long de cette
session ; (b) retrouvé et décompilé dans `Game.dll` (fonction `FUN_18085e870`,
adresse `0x18085e870`) — la logique assembleur correspond bit à bit à
l'implémentation Python ci-dessous.

### Clé statique XOR (64 octets, ASCII)

Adresse en mémoire (Ghidra, base statique `0x180000000`) : `0x180fd8100`

```
nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9
```

### Dérivation de la clé de session (`baseKey`)

À partir du champ `false_key` (4 octets) du paquet `SM_KEY`, envoyé en clair
(premier paquet serveur, non chiffré) :

```python
false_key = <4 octets du paquet SM_KEY, offset 7, little-endian>
baseKey = ((false_key - 0x3FF2CCDF) & 0xFFFFFFFF) ^ 0xCD92E4D9
```

### Clés roulantes par direction (8 octets chacune)

```python
b0, b1, b2, b3 = baseKey & 0xFF, (baseKey >> 8) & 0xFF, (baseKey >> 16) & 0xFF, (baseKey >> 24) & 0xFF
key_SERVER = key_CLIENT = bytearray([b0, b1, b2, b3, 0xA1, 0x6C, 0x54, 0x87])
```

Après chaque paquet chiffré/déchiffré dans une direction, la clé de cette
direction "roule" : on la traite comme un entier 64 bits little-endian et on
lui ajoute la taille (en octets) du paquet qui vient d'être traité.

### Algorithme de (dé)chiffrement (confirmé identique au code client)

```python
def decrypt(data, key, static_key):
    data = bytearray(data)
    prev = data[0]
    data[0] ^= key[0]
    for i in range(1, len(data)):
        curr = data[i]
        data[i] ^= static_key[i & 63] ^ key[i & 7] ^ prev
        prev = curr
    return bytes(data)
```

(Le chiffrement est la même opération en sens inverse — cipher auto-inverse
de type stream cipher.)

### Encodage/décodage de l'opcode serveur (S_)

Les paquets serveur ont leur opcode "obfusqué" sur le fil ; les paquets
client (C_) ne le sont pas.

```python
def decode_opcodec(wire):        # S2C uniquement
    return ((wire ^ 0xD9) - 0xD8) & 0xFFFF

def encode_opcodec(op):          # inverse, pour émettre du S2C
    return (op + 0xD8) ^ 0xD9
```

### Format d'un paquet sur le fil

```
[2 octets] longueur totale (little-endian, en clair, NON chiffrée)
--- début de la zone chiffrée ---
[2 octets] opcode (encodé pour S2C via encode_opcodec, brut pour C2S)
[1 octet]  marqueur direction : 0x56 = serveur->client, 0x75 = client->serveur
[2 octets] checksum (~opcode)
[N octets] payload
```

Confirmé dans le désassemblage (`FUN_18085e900`, la boucle de dispatch) :
après déchiffrement, le code saute exactement 5 octets (`param_3 = param_3 + 5`)
avant de traiter le payload — ce qui correspond exactement à opcode(2) +
marqueur(1) + checksum(2).

---

## 2. Autres clés trouvées sur le poste (non vérifiées contre notre trafic)

Ces clés proviennent d'une exploration antérieure (fichier `key_8.6.txt` sur
le Bureau). **Elles ne correspondent PAS au protocole de jeu S_/C_ ci-dessus**
(vérifié : ce ne sont pas les mêmes octets, et notre déchiffrement du trafic
de jeu fonctionne déjà sans elles). Elles appartiennent probablement à une
autre couche — authentification/login (AQ_/AC_), intégration plateforme
Gameforge (CLIGATE_), ou un canal TLS distinct. À vérifier si un jour on
s'attaque au protocole de login/auth ou à l'intégration boutique/launcher.

### Clé XOR (24 octets)

```
64 7A 89 99 ED 18 5F CA 4C 18 C6 78 4F 72 52 7E A3 D3 5E 38 7F 90 F0 19
```

### Clé Blowfish (16 octets)

```
C2 32 61 4E B7 5B E2 77 CE E3 DF 8F 57 E6 72 C3
```

### Clé RC4 / clé de session (8 octets)

```
C0 D0 E0 F0 E3 A6 01 F3
```

### Matériel TLS (probablement une poignée de main TLS 1.0/1.1 capturée ou analysée)

Vecteur d'initialisation (32 octets) :
```
18 D7 06 37 F8 C1 69 3A 2A 58 3E 7D 03 74 D5 0A 9F CA F4 9A 37 D2 AD 33 37 F3 59 A3 F0 AC 8B 9A
```

Note : `master secret`, `key expansion`, `server write key`, `client write key`,
`IV block` sont des **libellés standards du PRF TLS** (pas des clés en
elles-mêmes) — probablement juste les chaînes de contexte identifiées dans le
binaire, à ne pas confondre avec du matériel secret réel.

---

## 3. Emplacements utiles dans Game.dll (Ghidra, base statique 0x180000000)

| Élément | Adresse | Rôle |
|---|---|---|
| Clé XOR statique (données) | `0x180fd8100` | La chaîne de 64 octets elle-même |
| `FUN_18085e870` | `0x18085e870` | Fonction de (dé)chiffrement bas niveau (confirmée identique à notre Python) |
| `FUN_18085e710` | `0x18085e710` | Gestion bas niveau du buffer réseau (accumulation + décrypt), partagée par plusieurs systèmes |
| `FUN_18085e390` | `0x18085e390` | Boucle de réception/framing d'une connexion (reconstruction longueur, décrypt, décodage opcode) |
| `FUN_18085e900` | `0x18085e900` | Construit l'événement paquet {opcode, taille, données}, log `[GameServer] <nom>`, poste en file d'attente |
| `FUN_180826600` | `0x180826600` | Décodage de l'opcode à partir des octets bruts (équivalent probable de `decode_opcodec`) |
| `FUN_180672760` | `0x180672760` | Retourne la table opcode→nom (utilisée pour le log de debug `[GameServer] <nom>`) |
| Table opcode→nom S_ (map) | `0x1818831c0` | Table interne (arbre rouge-noir std::map) associant chaque opcode serveur à son nom textuel |
| Boucle d'enregistrement de la table S_ | `0x180672787` – `0x180679645` | ~417 blocs, un par opcode, chacun avec `LEA RDX,[nom]` suivi de `MOV [RSP+0x30], <opcode>` |

**Piste abandonnée (confirmée impasse) :** la file de messages générique
(`FUN_181da696d` et sa suite) forme un **cycle** après 52 sauts — c'est de la
plomberie générique du moteur (système pub/sub type-erased, probablement
partagé par des centaines de systèmes), pas un chemin qui mène au parsing
d'un paquet précis.

**Technique qui a marché : RTTI → vtable.** Le binaire contient les noms de
classes C++ en clair (RTTI, motif `.?AV<NomClasse>@@`). En remontant la
chaîne `TypeDescriptor → CompleteObjectLocator → vtable` (RTTI 64 bits MSVC),
on retrouve la vraie table de fonctions virtuelles d'une classe donnée sans
avoir à deviner. Confirmé sur `GameSocketHandler` : sa vtable contient
exactement `FUN_18085e390` (notre boucle de réception déjà identifiée) à
l'emplacement [4] — preuve que la technique fonctionne.

| Classe (RTTI) | Adresse du nom | vtable trouvée | Notes |
|---|---|---|---|
| `GameSocketHandler` | `0x1813d0138` | `0x18115d1f0` | [0]=erreur socket, [1..3]=Connect/Disconnect/Error (poussent vers la file générique), [4]=`FUN_18085e390` (réception/framing, confirmé), [5]=`FUN_18085e670` (flush buffer restant) |
| `SocketHandler` | `0x1813d0160` | non résolue (0 xref au COL trouvé) | classe de base probable de GameSocketHandler |
| `PacketParserHandler` | `0x1813d8c10` | résolue mais **hors-sujet** | dispatch sur seulement 7-10 cas (`switch(puVar2[1])`, `FUN_180c4e3d0`), pas les ~450 opcodes S_ — probablement un protocole différent (CEF/IPC/gateway, pas le protocole de jeu principal) |
| `TcpNodeHandler` | `0x1813d8c40` | résolue, chevauche celle de PacketParserHandler | à investiguer séparément si besoin |

**Autres classes RTTI repérées, non explorées :** `CligateHandler` (`0x1813d4b10`),
`NCTalkIpcServerHandler` (`0x1813d89f8`) — probablement respectivement
l'intégration plateforme Gameforge et l'IPC vers NCTalk, pas le protocole de
jeu.

**Statut de cette recherche Ghidra (session du 2026-07-22) :** le parsing
champ par champ d'un opcode S_/C_ précis (ex: `S_STATUS`) n'a **pas** été
localisé via l'analyse statique, malgré plusieurs techniques différentes
(traçage de la table de noms, traçage de la file de messages, recherche de
gros `switch`, recherche de singletons similaires, RTTI→vtable). Toutes les
briques AUTOUR du parsing (déchiffrement, framing, décodage d'opcode,
classe de connexion réelle) sont confirmées avec certitude.

**Le vrai déblocage est venu d'ailleurs : les captures existantes de
l'utilisateur** (`C:\Users\jmdbe\Desktop\capture_decrypt_8.6\`, des heures de
sessions officielles déjà enregistrées, pas besoin de débogueur). En
comparant `S_STATUS` à travers ~400 échantillons couvrant les niveaux 1 à
81 (plusieurs sessions), on a trouvé une transition nette : la valeur à
l'offset 230 (bloc "stats actuelles") et son miroir à l'offset ~538 (bloc
"stats de base") valent **0 pour niveau ≤ 50**, puis **(niveau − 50) × 6**
pile à partir du niveau 51 (confirmé sur les niveaux 51 à 81 sans
exception). Les deux existent déjà comme octets réservés dans notre
structure de 544 octets (juste codés en dur à 0 — la ligne source avait
même un commentaire `//40);// ??` trahissant un doute d'un dev précédent) ;
il manquait juste 4 octets de fin de paquet (toujours à 0 dans nos
échantillons). **Corrigé dans `SM_STATS_INFO.java`** : les deux `writeH(0)`
concernés écrivent maintenant `Math.max(0, (player.getLevel() - 50) * 6)`,
et un `writeD(0)` final a été ajouté pour porter le total à 548 octets
(confirmé identique au 8.6 réel). Nom exact du stat non confirmé à 100%
(candidat probable : `StatEnum.PHYSICAL_CRITICAL_REDUCE_RATE`, déclaré
dans `StatEnum.java` mais jamais utilisé ailleurs dans le code — cohérent
avec un stat post-50 jamais implémenté) — mais la valeur/formule/position
sont vérifiées sur des dizaines d'échantillons réels, donc le correctif
est fiable même sans le nom officiel.

Piste alternative si jamais ça ne suffit pas : analyse dynamique
(débogueur attaché à un client connecté au
serveur **officiel**, pour ne pas subir le crash qui affecte notre serveur
au level-up) — poser un point d'arrêt sur `FUN_18085e390`
(`0x18085e390`, boucle de réception confirmée) permettrait d'observer en
direct l'appel réel qui suit la mise en file d'attente.
