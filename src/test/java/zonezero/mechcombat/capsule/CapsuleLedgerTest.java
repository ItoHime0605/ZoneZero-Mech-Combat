package zonezero.mechcombat.capsule;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static zonezero.mechcombat.capsule.CapsuleLedger.*;

public final class CapsuleLedgerTest {
    private static int passed = 0;
    private static void yes(boolean b, String test) {
        if (!b) throw new AssertionError(test);
        passed++;
    }
    private static void fails(Throwing task, String msg) throws Exception {
        try { task.run(); throw new AssertionError(msg); }
        catch (IllegalArgumentException | IllegalStateException expected) { passed++; }
    }
    interface Throwing { void run() throws Exception; }

    public static void main(String[] args) throws Exception {
        final List<Entry> persisted = new ArrayList<>();
        Store storage = persisted::add;
        CapsuleLedger l = new CapsuleLedger(storage, List.of());
        UUID c = UUID.randomUUID(), owner = UUID.randomUUID(), part1 = UUID.randomUUID(), part2 = UUID.randomUUID();
        fails(() -> l.beginCapture(c,owner,"minecraft:overworld",List.of(part1,part1)), "reject duplicate sublevels");
        fails(() -> l.beginCapture(c,owner,"minecraft:overworld",List.of()), "reject empty machine");
        Entry e = l.beginCapture(c,owner,"minecraft:overworld",List.of(part1,part2));
        yes(e.phase() == Phase.PACKING, "packing persisted");
        yes(e.subLevels().size() == 2, "entire walker chain captured");
        fails(() -> l.beginCapture(UUID.randomUUID(), owner,"minecraft:overworld",List.of(part2)), "prevent double capture");
        fails(() -> l.finishDeploy(c), "no deployment from packing");
        l.finishCapture(c);
        yes(l.canDeploy(c,owner), "owner may deploy packed item");
        yes(!l.canDeploy(c,UUID.randomUUID()), "stranger cannot deploy");
        fails(() -> l.beginDeploy(c,UUID.randomUUID()), "stranger rejected");
        l.beginDeploy(c,owner);
        fails(() -> l.beginDeploy(c,owner), "duped item blocked");
        yes(l.find(c).orElseThrow().phase() == Phase.DEPLOYING, "world restore in progress");
        l.finishDeploy(c);
        yes(!l.canDeploy(c,owner), "single-use token disabled");
        fails(() -> l.beginDeploy(c,owner), "second deployment blocked");
        fails(() -> l.beginCapture(c,owner,"minecraft:overworld",List.of(UUID.randomUUID())), "cannot recycle used token ID");
        final AtomicBoolean fail = new AtomicBoolean(true);
        CapsuleLedger io = new CapsuleLedger(record -> { if(fail.get()) throw new java.io.IOException("disk full"); }, List.of());
        UUID ioId = UUID.randomUUID();
        try { io.beginCapture(ioId,owner,"minecraft:overworld",List.of(UUID.randomUUID())); throw new AssertionError(); }
        catch (java.io.IOException ex) { passed++; }
        yes(io.find(ioId).isEmpty(), "failed write must not reserve mech");
        fail.set(false);
        UUID q = UUID.randomUUID(), qp = UUID.randomUUID();
        io.beginCapture(q,owner,"minecraft:overworld",List.of(qp));
        io.quarantine(q);
        fails(() -> io.beginDeploy(q,owner), "quarantined mech cannot deploy");
        fails(() -> io.resetUndeployed(q,false), "reconcile needs world verification");
        io.resetUndeployed(q,true);
        yes(io.canDeploy(q,owner), "verified recovery allows retry");
        CapsuleLedger restarted = new CapsuleLedger(storage, List.of(l.find(c).orElseThrow()));
        yes(!restarted.canDeploy(c,owner), "reboot preserves token tombstone");
        yes(persisted.size() >= 4, "all transitions persisted");
        System.out.println("PASS: " + passed + " capsule transaction assertions");
    }
}
