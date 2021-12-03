
PRINCIPAL= sbc

TEXTOS= $(PRINCIPAL).tex

.PHONY : pdf clean


$(PRINCIPAL).pdf : $(PRINCIPAL).bbl $(TEXTOS)
	pdflatex --shell-escape $(PRINCIPAL).tex ; pdflatex --shell-escape $(PRINCIPAL).tex


$(PRINCIPAL).bbl : $(PRINCIPAL).bib
	pdflatex --shell-escape $(PRINCIPAL).tex ; bibtex $(PRINCIPAL) ; pdflatex --shell-escape $(PRINCIPAL).tex


clean :
	rm -f *~ *.dvi *.ps *.pdf *.aux *.log *.toc *.bbl *.blg *.out \
              *.dia.eps *.plot.eps *.nav *.snm
