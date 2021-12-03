### Makefile
### $Id: Makefile,v 1.11 2011/01/17 14:13:29 tiago Exp $
all: uninstall fellipe.ugliara.dvi fellipe.ugliara.ps fellipe.ugliara.pdf clean

uninstall:
	rm -f fellipe.ugliara.pdf

fellipe.ugliara.dvi: fellipe.ugliara.tex
		latex  fellipe.ugliara
		bibtex fellipe.ugliara
		latex  fellipe.ugliara
		latex  fellipe.ugliara

fellipe.ugliara.ps: fellipe.ugliara.dvi
		dvips -Z -Pamz -Pcmz -Ppdf -G0 fellipe.ugliara.dvi -o fellipe.ugliara.ps

fellipe.ugliara.pdf: fellipe.ugliara.ps
		ps2pdf -dOptimize=true -dEmbedAllFonts=true -dPDFSETTINGS=/printer fellipe.ugliara.ps

clean:
	rm -f core *.core *.log *.aux *.toc *.lo[fpta] *.blg \
	*.ind *.ilg *.idx *.glo *.gls *.out *~ *.backup *.ps *.dvi

### Makefile ends here.

