
The collection reader defined by GeniaPosGoldReader parses data from a file released by Genia that has the 
following name: GENIAcorpus3.02.pos.xml.  This file as distributed has a number of mistakes in it that should
be fixed manually before being used by GeniaPosGoldReader.  

The following edits should be made to the file GENIAcorpus3.02.pos.xml because they 
look to be mistakes:

Line 6878 replaced:
<w c="*">Pan/</w>E2A 
with
<w c="*">Pan/</w><w c="NN">E2A</w>
Line 7954 replaced:
</w>.
with
</w><w c=".">.</w>
Line 7955 replaced:
</w>.
with
</w><w c=".">.</w>
Line 8019 replaced:
</w> HMG-I(Y)
with
</w> <w c="NN">HMG-I(Y)</w>
Line 27459 replaced:
</w>-
with
</w><w c="NN">-</w>

7955 replace:
<w c="">IFN-gamma</w>
with:
<w c="NN">IFN-gamma</w>
12357 replace:
<w c="">)</w>
<w c=")">)</w>


