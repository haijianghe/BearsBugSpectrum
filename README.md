# BearsBugSpectrum
Ubuntu+VirtualBox下收集Bears 的程序谱及源代码

https://github.com/bears-bugs/bears-benchmark （Bears 数据集）    
执行我的程序，收集每个测试用例的覆盖数据。
最终产生了程序谱。
为研究静态代码对软件错误定位性能的影响，每个数据集除程序谱矩阵外，还附带了相应的源代码。

此外，github 有功能:
(直接在github页面上删除文件。) 简单的github创建文件夹的方法，首先是进入所要创建文件夹的库中点击Create new file，
然后在这里输入你要创建的文件夹名称（此时我们创建的其实还是一个文件而不是文件夹）
接着是继续输入当我们按下一个“/”后就变成下面的样子了，此时new file 就变成了一个文件夹了
最后一步（因为github不允许创建空文件夹）我们需要在新文件夹下创建一个文件，用了readme.txt.  

If you use Bears, please cite paper:

@inproceedings{Madeiral2019,
  author = {Fernanda Madeiral and Simon Urli and Marcelo Maia and Martin Monperrus},
  title = {{Bears: An Extensible Java Bug Benchmark for Automatic Program Repair Studies}},
  booktitle = {Proceedings of the 26th IEEE International Conference on Software Analysis, Evolution and Reengineering (SANER '19)},
  year = {2019},
  url = {https://arxiv.org/abs/1901.06024}
}
