                        DMIE 1.0.0

����Server
--��ȷ�������Ѱ�װ Python2.7
--��ȷ���Ѱ�װ Python ģ�� twisted
  �Ƽ�ʹ�� pip ��װPythonģ��
  pip install twisted

--�������úú󣬼�����������
--���� Jeffery �ļ��У���������
  python main.py



���ӷ���
--��ȷ�������Ѱ�װJava
--���� Sherly �ļ��У�����������û��.class�ļ�����Ҫ���б���һ�Σ�
  [�ն�1]java Client client1 client2
  [�ն�2]java Client client2 client1

--���ͣ����в���arg1, arg2 �ֱ�Ϊ�û�������Ϣ���Ͷ���Ҳ����˵���ն�1���е�Ч����client1������Ϣ��client2
--ע�⣺������һ��clientʱ����ȷ��arg2����ʾ���û������Ϸ���


-------------------------------2016.7.5����-----------------------------------
mainServer��
--���xml�����ļ����ṩ����nodeServer��host, port����������Ϣ
--���mainServer����Э�飺
  001:���������񣬷���nodeServer��host, port��
  ���ࣺ������
--issue:ʵʱ�����㷨�����Ÿ���Э����룬���������ļ��ĸ�ʽ
--������python main.py [configure.xml] (����ָ�������ļ�)


nodeServer��
--����client���ӣ�������ʾ��Ϣ
--issue:����mainServer���ṩ���ݣ�������client���ݴ���Э�飬ʵ��client����Ϣͨ��
--������python node.py xxxx (�˿ںţ����������ļ���һ��)


--Client:
--�޸�Ϊ������mainServer,������nodeServer
--ɾ���������룬�Ժ����
--issue���ж�ÿһ�������������Ľ��������ܴ������ݣ���nodeServerЭ�̣���������׿�����
--������java Client 


