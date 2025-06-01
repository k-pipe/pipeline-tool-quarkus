package com.kneissler.util.html;

public class TestHtml {

	public static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud ex er cit at ion ul la mco laboris nisi ut aliquip ex ea commodo consequat.";
	
	public static void main(String... args) {
		HTMLDocument doc = HTML.createDocument("TestDoc");
		HTMLSection sec1 = doc.addSection("Section 1",HTMLColor.GREEN);
		sec1.addParagraph(TEXT);
		HTMLConsole console = sec1.addConsole();
		console.addLine("kjdekc lekjckj l l kl jlkjölcjdlj lkdjlkjdlkjlkdj lkjdjkdlkllj lk jl jld jdl jdljdl jdl j ld jljdljdldljdljdl  jdl jdlj l");
		console.addLine("sudo rm -rf /",HTMLColor.RED);
		console.addLine("ls -l",HTMLColor.GREEN);
		console.addLine("du -s",HTMLColor.GRAY);
		console.addLine("du -s",HTMLColor.CYAN);
		HTMLSection sec2 = doc.addSection("Section 2",HTMLColor.RED);
		HTMLSection subsec2a = sec2.addSubSection("Sub Section a", HTMLColor.GRAY);
		subsec2a.addParagraph(TEXT);
		HTMLSection subsec2b = sec2.addSubSection("Sub Section b", HTMLColor.RED);
		subsec2b.addParagraph(TEXT);
		HTMLSection subsec2c = sec2.addSubSection("Sub Section c", HTMLColor.BLUE);
		subsec2c.addParagraph(TEXT);
		HTMLConsole console2 = subsec2c.addConsole();
		console2.addLine("kjdekc lekjckj l l kl jlkjölcjdlj lkdjlkjdlkjlkdj lkjdjkdlkllj lk jl jld jdl jdljdl jdl j ld jljdljdldljdljdl  jdl jdlj l");
		console2.addLine("sudo rm -rf /");
		subsec2c.addParagraph(TEXT);
		subsec2c.addSubSection("SubSubSec", HTMLColor.BLUE).addParagraph(TEXT);
		doc.saveAs("C:\\tmp\\test.html");
	}
	
}
