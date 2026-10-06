//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.samskivert.util.StringUtil;

import static com.threerings.io.StreamableTest.*; // for Wocket, Wackable, Wacket, (un)flatten

/**
 * Tests the streaming of records.
 */
public class RecordStreamerTest
{
  public record Point (int x, int y) implements Streamable {}

  public record Pair<L, R> (L left, R right) implements Streamable {}

  public record Empty () implements Streamable {}

  public record Kitchen (
    boolean bool1, byte byte1, char char1, short short1, int int1, long long1, float float1,
    double double1, Integer boxedInt, Integer nullBoxedInt, String string1, String nullString1,
    @Intern String interned, Date date1, Class<?> class1, int[] ints, Object[] objects,
    Point point, Point nullPoint, Point[] points, Wocket wocket, Wackable wackable,
    Object object1, List<Integer> list, Set<String> set, Map<String, Integer> map,
    Pair<String, Integer> pair)
    implements Streamable {}

  public record Wire (
    int count, String name, @Intern String kind, Integer none, Point point, List<Integer> list)
    implements Streamable {}

  public static class Holder extends SimpleStreamableObject
  {
    public Point point = new Point(1, 2);
    public Point[] points = { new Point(3, 4), null };
  }

  public record Before (int count) implements Streamable {}

  public record After (int count, String name, long stamp) implements Streamable {
    public After {
      if (name == null) name = "unnamed";
    }
  }

  public record Lax (String name) implements Streamable {}

  public record Strict (String name) implements Streamable {
    public Strict {
      Objects.requireNonNull(name);
    }
  }

  record Hidden (int x) implements Streamable {}

  public record Custom (int x) implements Streamable {
    public void writeObject (ObjectOutputStream out)
      throws IOException
    {
      out.writeInt(x);
    }
  }

  public record Unmarshallable (Thread thread) implements Streamable {}

  @Test
  public void testRoundTrip ()
    throws Exception
  {
    Kitchen kitchen = new Kitchen(
      true, Byte.MAX_VALUE, 'a', Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE,
      Float.MAX_VALUE, Double.MAX_VALUE, Integer.MIN_VALUE, null, "one", null, "monkey butter",
      new Date(42L), Point.class, new int[] { 1, 2, 3 }, new Object[] { new Point(5, 6), "str" },
      new Point(1, 2), null, new Point[] { new Point(3, 4), null, new Point(5, 6) },
      new Wocket(), new Wacket("for"), new Point(7, 8),
      List.of(1, 2, 3), Set.of("a", "b"), Map.of("one", 1, "two", 2), new Pair<>("left", 2));
    Kitchen read = (Kitchen)unflatten(flatten(kitchen));
    assertComponentsEqual(kitchen, read);
    // the component was streamed as an intern, which reads back as the pooled instance
    assertSame("monkey butter", read.interned());

    assertEquals(new Empty(), unflatten(flatten(new Empty())));
  }

  @Test
  public void testRecordsInClass ()
    throws IOException, ClassNotFoundException
  {
    Holder holder = new Holder();
    Holder read = (Holder)unflatten(flatten(holder));
    assertEquals(holder.point, read.point);
    assertArrayEquals(holder.points, read.points);
  }

  @Test
  public void testWireFormat ()
    throws IOException
  {
    Wire wire = new Wire(7, "seven", "kind", null, new Point(1, 2), List.of(3));

    // spell out the expected stream with nothing but a DataOutputStream
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(bout);
    dout.writeShort(-1); // Wire's class code, negated because its name follows
    dout.writeUTF(Wire.class.getName());
    dout.writeInt(7); // count
    dout.writeBoolean(true); // name is non-null
    dout.writeUTF("seven");
    dout.writeShort(-1); // kind's intern code, negated because its value follows
    dout.writeUTF("kind");
    dout.writeBoolean(false); // none is null
    dout.writeShort(-2); // point's class code and name
    dout.writeUTF(Point.class.getName());
    dout.writeInt(1); // point.x
    dout.writeInt(2); // point.y
    dout.writeBoolean(true); // list is non-null
    dout.writeInt(1); // list size
    dout.writeShort(-3); // the element's class code and name
    dout.writeUTF(Integer.class.getName());
    dout.writeInt(3);

    assertEquals(StringUtil.hexlate(bout.toByteArray()), StringUtil.hexlate(flatten(wire)));
  }

  @Test
  public void testMissingComponents ()
    throws IOException, ClassNotFoundException
  {
    // a stream written before After gained its trailing components passes them as zero/null
    assertEquals(new After(3, "unnamed", 0L), unflatten(flattenAs(new Before(3), After.class)));
  }

  @Test
  public void testConstructorRejection ()
    throws IOException, ClassNotFoundException
  {
    byte[] data = flattenAs(new Lax(null), Strict.class);
    try {
      unflatten(data);
      fail("Strict accepted a null name");
    } catch (IOException ioe) {
      assertTrue(ioe.getCause() instanceof NullPointerException);
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void testNonPublicFail ()
    throws IOException
  {
    flatten(new Hidden(1));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testCustomWriterFail ()
    throws IOException
  {
    flatten(new Custom(1));
  }

  @Test(expected=RuntimeException.class)
  public void testUnmarshallableFail ()
    throws IOException
  {
    flatten(new Unmarshallable(Thread.currentThread()));
  }

  /**
   * Streams the supplied object as if it were an instance of the supplied class.
   */
  protected static byte[] flattenAs (Object object, Class<?> as)
    throws IOException
  {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream oout = new ObjectOutputStream(bout);
    oout.addTranslation(object.getClass().getName(), as.getName());
    oout.writeObject(object);
    return bout.toByteArray();
  }

  /**
   * Asserts that the components of the supplied records are equal, comparing arrays by content.
   */
  protected static void assertComponentsEqual (Record expected, Record actual)
    throws Exception
  {
    assertEquals(expected.getClass(), actual.getClass());
    for (var comp : expected.getClass().getRecordComponents()) {
      var accessor = comp.getAccessor();
      assertTrue(comp.getName(),
        Objects.deepEquals(accessor.invoke(expected), accessor.invoke(actual)));
    }
  }
}
