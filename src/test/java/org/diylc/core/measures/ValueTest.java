import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ValueTest {
  @Test
  public void parseTest() {
    // voltage
    assertEquals(Value.parse("1 V"), new Value(1, SiUnit.VOLT));
    assertEquals(Value.parse("1.0 V"), new Value(1, SiUnit.VOLT));
    assertEquals(Value.parse("1.01 V"), new Value(1.01, SiUnit.VOLT));
    assertEquals(Value.parse("1.01V"), new Value(1.01, SiUnit.VOLT));
    assertEquals(Value.parse("2 mV"), new Value(0.002, SiUnit.VOLT));
    // resistance
    assertEquals(Value.parse("3r"), new Value(3, SiUnit.OHM));
    assertEquals(Value.parse("3R"), new Value(3, SiUnit.OHM));
    assertEquals(Value.parse("3 r"), new Value(3, SiUnit.OHM));
    assertEquals(Value.parse("3 R"), new Value(3, SiUnit.OHM));
    assertEquals(Value.parse("3 Ω"), new Value(3, SiUnit.OHM));
    assertEquals(Value.parse("3.0 Ω"), new Value(3, SiUnit.OHM));
    assertEquals(Value.parse("3.0mΩ"), new Value(0.003, SiUnit.OHM));
    assertEquals(Value.parse("3.0 mΩ"), new Value(0.003, SiUnit.OHM));
    assertEquals(Value.parse("3.0kΩ"), new Value(3000, SiUnit.OHM));
    assertEquals(Value.parse("3.0 kΩ"), new Value(3000, SiUnit.OHM));
    assertEquals(Value.parse("3.1MΩ"), new Value(3100000, SiUnit.OHM));
    assertEquals(Value.parse("3.1 MΩ"), new Value(3100000.0, SiUnit.OHM));
    // inductance
    assertEquals(Value.parse("4H"), new Value(4, SiUnit.HENRY));
    assertEquals(Value.parse("4 H"), new Value(4, SiUnit.HENRY));
    assertEquals(Value.parse("4mH"), new Value(0.004, SiUnit.HENRY));
    assertEquals(Value.parse("4 mH"), new Value(0.004, SiUnit.HENRY));
    // capacitance
    assertEquals(Value.parse("5F"), new Value(5, SiUnit.FARAD));
    assertEquals(Value.parse("5 F"), new Value(5, SiUnit.FARAD));
    assertEquals(Value.parse("5  F"), new Value(5, SiUnit.FARAD));
    assertEquals(Value.parse(" 5  F"), new Value(5, SiUnit.FARAD));
    assertEquals(Value.parse("5mF"), new Value(.005, SiUnit.FARAD));
    assertEquals(Value.parse("5 mF"), new Value(.005, SiUnit.FARAD));
    assertEquals(Value.parse("5  mF"), new Value(.005, SiUnit.FARAD));
    assertEquals(Value.parse(" 5  mF"), new Value(.005, SiUnit.FARAD));
    assertEquals(Value.parse("5µF"), new Value(5e-9, SiUnit.FARAD));
    assertEquals(Value.parse("5 µF"), new Value(5e-9, SiUnit.FARAD));
    assertEquals(Value.parse("5.1  µF"), new Value(5.1e-9, SiUnit.FARAD));
    assertEquals(Value.parse(" 5.1  µF"), new Value(5.1e-9, SiUnit.FARAD));
    assertEquals(Value.parse("5.2nF"), new Value(5.2e-9, SiUnit.FARAD));
    assertEquals(Value.parse("5.2 nF"), new Value(5.2e-9, SiUnit.FARAD));
    assertEquals(Value.parse("5.22  nF"), new Value(5.22e-9, SiUnit.FARAD));
    assertEquals(Value.parse(" 5.23  nF"), new Value(5.23e-9, SiUnit.FARAD));
    assertEquals(Value.parse("5.3pF"), new Value(5.3e-12, SiUnit.FARAD));
    assertEquals(Value.parse("5.33 pF"), new Value(5.33e-12, SiUnit.FARAD));
    assertEquals(Value.parse("5.333  pF"), new Value(5.333e-12, SiUnit.FARAD));
    assertEquals(Value.parse(" 5.3333  pF"), new Value(5.3333e-12, SiUnit.FARAD));
  }
}
