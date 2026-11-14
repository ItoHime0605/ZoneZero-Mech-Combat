package zonezero.mechcombat.capsule;

import java.nio.file.*;
import java.util.*;

public final class CapsuleDiskStoreTest {
  private static int passed = 0;
  private static void ok(boolean value, String test) { if (!value) throw new AssertionError(test); passed++; }
  public static void main(String[] args) throws Exception {
    Path d = Files.createTempDirectory("mech-storage-test");
    try {
      CapsuleDiskStore s = new CapsuleDiskStore(d);
      UUID id = UUID.randomUUID(), owner = UUID.randomUUID(), body = UUID.randomUUID(), left = UUID.randomUUID();
      byte[] data = new byte[] {1,2,3,4,5,6};
      s.archiveSnapshot(id, data);
      ok(Arrays.equals(data,s.loadSnapshot(id)), "snapshot roundtrip");
      try { s.archiveSnapshot(id, data); throw new AssertionError(); } catch (FileAlreadyExistsException expected) { passed++; }
      try { s.archiveSnapshot(UUID.randomUUID(),new byte[0]); throw new AssertionError(); } catch (java.io.IOException expected) { passed++; }
      CapsuleLedger ledger = new CapsuleLedger(s,List.of());
      ledger.beginCapture(id,owner,"minecraft:overworld",List.of(body,left));
      ok(s.loadRecords().size()==1,"entry persisted");
      ledger.finishCapture(id);
      CapsuleLedger reloaded = new CapsuleLedger(s,s.loadRecords());
      ok(reloaded.canDeploy(id,owner),"packed token survives restart");
      reloaded.beginDeploy(id,owner);
      reloaded.finishDeploy(id);
      CapsuleLedger after = new CapsuleLedger(s,s.loadRecords());
      ok(!after.canDeploy(id,owner),"used capsule cannot deploy after restart");
      Path payload = d.resolve("data/zonezero_mechcombat/capsules/archives/"+id+".dat");
      byte[] blob = Files.readAllBytes(payload); blob[blob.length-1] ^= 1;
      Files.write(payload,blob);
      try { s.loadSnapshot(id); throw new AssertionError(); } catch (java.io.IOException expected) { passed++; }
      System.out.println("PASS: "+passed+" disk store assertions");
    } finally { // test fixture only
      try(var walk=Files.walk(d)) { walk.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(Exception e){}}); }
    }
  }
}
