
public class Util {
	public static String formatTime(long ts) {
		long s = ts % 60;
		long tm = ts / 60;
		long m = tm % 60;
		long th = m / 60;
		long h = th % 24;
		long d = th / 24;

		String days = (d == 0) ? "" : (d == 1) ? "1 dia, " : String.format("%d dias, ", d);
		String hours = (h == 0) ? "" : (h == 1) ? "1 hora, " : String.format("%d horas, ", h);
		String minutes = (m == 0) ? "" : (m == 1) ? "1 minuto, " : String.format("%d minutos, ", m);
		String seconds = (s == 0) ? "" : (s == 1) ? "1 segundo, " : String.format("%d segundos, ", s);

		String time = days + hours + minutes + seconds;

		// remove a útlima vírgula
		return replaceLast(replaceLast(time, ",", ""), ",", " e");
	}
	
	private static String replaceLast(String text, String substring, String replacement)
	{
	  int i = text.lastIndexOf(substring);
	  if (i == -1)
	    return text;
	  else
		  return text.substring(0, i) + replacement + text.substring(i + substring.length());
	}
}
