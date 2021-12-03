
PRINCIPAL= cyan.metaobjects.treplica

TEXTOS= $(PRINCIPAL).tex

FIGURAS=


.PHONY : pdf ps dvi clean


pdf : $(PRINCIPAL).pdf

$(PRINCIPAL).pdf : $(PRINCIPAL).ps
	ps2pdf $(PRINCIPAL).ps


ps : $(PRINCIPAL).ps

$(PRINCIPAL).ps : $(PRINCIPAL).dvi
	dvips -P pdf $(PRINCIPAL).dvi


dvi : $(PRINCIPAL).dvi

$(PRINCIPAL).dvi : $(FIGURAS) $(PRINCIPAL).bbl $(TEXTOS)
	latex $(PRINCIPAL).tex ; latex $(PRINCIPAL).tex


%.dia.eps : figuras/%.dia
	dia -n -e $@ figuras/`basename $@ .eps`

%.plot.eps : graficos/%.plot graficos/*data
	cd graficos ; gnuplot `basename $@ .eps` ; \
	cd .. ; if [ $$OSTYPE == 'linux-gnu' ]; then \
	     sed -i '/^SDict begin \[$$/,/^end$$/d' $@; \
	else sed -i "" '/^SDict begin \[$$/,/^end$$/d' $@; fi


$(PRINCIPAL).bbl :  $(PRINCIPAL).bib
	latex $(PRINCIPAL).tex ; bibtex $(PRINCIPAL) ; latex $(PRINCIPAL).tex


clean :
	rm -f *~ *.dvi *.ps *.pdf *.aux *.log *.toc *.bbl *.blg *.out \
              *.dia.eps *.plot.eps *.nav *.snm comment.cut *.spl
