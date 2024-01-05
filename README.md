# PermLegend
    Kleines aber feines Plugin für die Verwaltung von Permissions innerhalb von Gruppen.
    Dieses Plugin erlaubt eine sehr umfangreiche steuerung der Gruppen und anderen Funktionen durch eine große Vielfalt von Commands.
    
### Gruppen
    Standardmäig wird die Gruppe "Default" erstellt und alle Spieler haben, sofern nicht anders gesetzt diese Gruppe.
    Weitere Gruppen können jederzeit per Command (Console/Spieler) erstellt und gesteuert werden.

    Gruppen haben folgende Features:
    - Prefix (Wird im Chat / beim Joinen im Chat angezeigt)
    - Suffix (Wird im Chat hinter der Spieler Nachricht angezeigt)
    - ChatColor (Die Nachrichten des Spielers sind automatisch in dieser Farbe)
    - Permissions (Permissions können per Command oder per Config-Bearbeitung geändert werden, Spieler erhalten direkt automatisch beim Joinen alle Permissions)
    - Sibling (Vererbte Permissions: Permissions der Sibling-Gruppe)

### SignDisplays
    Schilder (egal ob Stehend, an der Wand oder an der Decke) können die Gruppe und den dazugehörigen Spielernamen anzeigen
    - Erstellt werden sie mit dem Command '/permission setSignDisplay <Player>' (Der Spieler muss dabei das Schild direkt ankucken)


### Nachrichten
    Alle Nachrichten, wie z.B. beim Joinen, bei Chat-Nachrichten, oder bei Commands, können durch die Configurations-Datei angepasst werden.
    Nachrichten sowie Prefix oder Suffix Nachrichten können mit dem Color-System von Minecraft genutzt werden. z.B.: &4 -> Rot, &2 -> Grün.

### Permissions
    Die Permissions basieren auf dem exestierenden Vanilla-Permissions-System, was bedeutet:
    - Alle Permissions von anderen Plugins sowie die Vanilla Permissions können hinzugefügt werden.
    - Über die Permission '*' können dem Spieler einer Gruppe automatisch alle exestierenden Permissions hinzugefügt werden
    - Über die Permission '-*' werden automatisch alle Permissions dem Spieler entzogen
    - Permissions die mit '-' anfangen werden ebenfalls dem Spieler entzogen

### Commands
    Das Argument '<Group>' kann genutzt werden um exestierende Gruppen auszuwählen. Bei dem Befehl '/permission createGroup <Group> "Prefix"' werden zwar die exestierenden Gruppen als Vorschau angezeigt, aber können nicht mit dem gleichen Namen erstellt werden.

    Das Argument 'Player' akzeptiert entweder Spielernamen, mit einer Zeichenlänge von 16 oder weniger - oder eine UUID im Format '9a25bce6-eb7d-48e8-8d46-a54d526b6f18'
    - Bei einem Spielername muss der Spieler Online sein
    - Bei der UUID reicht es, wenn der Spieler bereits exestiert hat

    Die Argumente '"Prefix"' und '"Suffix"' müssen mit Anführungszeichen im folgenden Format genutzt werden:
    - "Text1 &1Text2 &fText3"
    - "Text1"
    - "" (=Kein Prefix/Suffix)
    Nachrichten sowie Prefix oder Suffix Nachrichten können mit dem Color-System von Minecraft genutzt werden. z.B.: &4 -> Rot, &2 -> Grün.

    Das Argument '<Time>' kann folgende Inputs akzeptieren:
    - 5d 10h 3m 20s
    # 5 Tage, 10 Stunden, 3 Minuten, 20 Sekunden
    - 5d 20s
    # 5 Tage, 20 Sekunden
    - 3s
    # 3 Sekunden
    - 5000
    # 5000 Sekunden

    Die Argumente '<Color>', '<Permission>' und '<Page>' sind jeweils nur einzelne Argumente.
    z.B.:
    - &4
    # für <Color>
    - permissions.abc.test
    # für <Permission>
    - 3
    # für <Page>

##### Gruppen-Commands
    /permission listGroups
    /permission createGroup <Group> "Prefix"
    /permission getGroupInfo <Group>
    /permission setPrefix <Group> "Prefix"
    /permission setSuffix <Group> "Suffix"
    /permission setChatColor <Group> <Color>
    /permission addPermission <Group> <Permission>
    /permission removePermission <Group> <Permission>
    /permission listPermissions <Group> <Page>

##### Player-Commands
    /permission info <Player>
    /permission hasPermission <Player> <Permission>
    /permission setGroup <Player> <Group>
    /permission setTempGroup <Player> <Group> <Time>

    /permission setSignDisplay <Player>
    # Siehe unter Punkt 'SignDisplays'
